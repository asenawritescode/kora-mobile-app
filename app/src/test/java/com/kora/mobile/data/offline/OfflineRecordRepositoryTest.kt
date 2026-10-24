package com.kora.mobile.data.offline

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kora.mobile.core.database.*
import com.kora.mobile.core.security.PayloadCipher
import com.kora.mobile.domain.model.*
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], application = android.app.Application::class)
class OfflineRecordRepositoryTest {
    private lateinit var db: KoraDatabase
    private lateinit var repo: OfflineRecordRepository
    private val scope = Scope("test-instance", "https://kora.invalid", "test-shop", "owner")
    private val cipher = object : PayloadCipher {
        override fun encrypt(value: String) = value
        override fun decrypt(encoded: String) = encoded
    }
    private val remote = FakeRemote()

    @Before fun before() {
        db = Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), KoraDatabase::class.java).build()
        repo = OfflineRecordRepository(db, cipher, remote)
    }
    @After fun after() { db.close() }

    @Test fun localSaveAppearsImmediatelyWithoutNetwork() = runBlocking {
        val id = save("Milk")
        assertEquals(0, remote.calls)
        assertEquals("Milk", repo.records(scope, "Product").getJSONObject(0).getString("product_name"))
        assertEquals("pending", repo.record(scope, "Product", id)!!.getString("_sync_status"))
        assertEquals(1, db.localDao.queuedMutations(scope.key).size)
    }

    @Test fun acknowledgementMapsIdentityAndPreservesList() = runBlocking {
        val id = save("Milk")
        val result = repo.sync(scope)
        assertEquals(1, result.acknowledged)
        assertNull(result.error)
        assertTrue(db.localDao.queuedMutations(scope.key).isEmpty())
        val record = repo.record(scope, "Product", id)!!
        assertEquals("SERVER-1", record.getString("name"))
        assertEquals(id, record.getString("_local_id"))
        assertEquals(1, repo.records(scope, "Product").length())
    }

    @Test fun rejectedOperationDoesNotBlockUnrelatedRecord() = runBlocking {
        remote.rejectName = "Invalid"
        save("Invalid"); save("Valid")
        val result = repo.sync(scope)
        assertEquals(1, result.acknowledged)
        assertEquals("rejected", db.localDao.queuedMutations(scope.key).single().status)
        assertEquals(2, repo.records(scope, "Product").length())
    }

    @Test fun concurrentSyncTriggersSendEachOperationOnce() = runBlocking {
        save("Milk")
        coroutineScope { awaitAll(async { repo.sync(scope) }, async { repo.sync(scope) }) }
        assertEquals(1, remote.calls)
        assertEquals(1, remote.effects.size)
    }

    @Test fun editDuringUploadSurvivesAcknowledgementAndUsesServerId() = runBlocking {
        val id = save("Original")
        remote.duringSend = {
            repo.save(scope, "Product", id, JSONObject().put("product_name", "New edit"), "edit-key", "New edit")
            db.localDao.putDraft(LocalDraft("later-draft", scope.key, "Product", id, "{}", 2, 2))
        }
        repo.sync(scope)
        assertEquals("New edit", repo.record(scope, "Product", id)!!.getString("product_name"))
        assertEquals(1, db.localDao.queuedMutations(scope.key).size)
        assertNotNull(db.localDao.getDraft(scope.key, "later-draft"))
        repo.sync(scope)
        assertEquals("SERVER-1", remote.sent.last().second)
        assertTrue(db.localDao.queuedMutations(scope.key).isEmpty())
    }

    @Test fun lostAcknowledgementReplaysSameOperationWithoutDuplicateEffect() = runBlocking {
        remote.loseResponseOnce = true
        save("Milk")
        assertNotNull(repo.sync(scope).error)
        assertEquals(1, repo.sync(scope, force = true).acknowledged)
        assertEquals(1, remote.effects.size)
        assertEquals(remote.sent[0].first, remote.sent[1].first)
    }

    @Test fun downloadsAllPagesAndKeepsPendingOverlay() = runBlocking {
        remote.rows = (1..205).map { JSONObject().put("name", "P-$it").put("product_name", "Item $it").put("_version", "v1") }
        repo.refresh(scope, "Product")
        assertEquals(205, repo.records(scope, "Product").length())
        repo.save(scope, "Product", "P-1", JSONObject().put("product_name", "Local edit"), "local-edit", null)
        repo.refresh(scope, "Product")
        assertEquals("Local edit", repo.record(scope, "Product", "P-1")!!.getString("product_name"))
        assertEquals(listOf(0, 100, 200, 205, 0, 100, 200, 205), remote.offsets)
    }

    @Test fun linkedLocalRecordResolvesAfterItsCreateIsAcknowledged() = runBlocking {
        val product = save("Milk")
        repo.save(scope, "Sale", null, JSONObject().put("product", product), "sale-key", null)
        assertEquals(2, repo.sync(scope).acknowledged)
        assertEquals("SERVER-1", remote.bodies.last().getString("product"))
    }

    @Test fun cancellationKeepsDurableOperationForReplay() = runBlocking {
        save("Milk")
        remote.cancel = true
        try { repo.sync(scope); fail("Expected cancellation") } catch (_: CancellationException) { }
        assertEquals("sent", db.localDao.queuedMutations(scope.key).single().status)
        remote.cancel = false
        assertEquals(1, repo.sync(scope, true).acknowledged)
    }

    @Test fun importsLegacyCacheAndOutboxWithoutChangingIdempotencyKey() = runBlocking {
        db.localDao.putEntry(LocalEntry(scope.key, "records:Product", "[{\"name\":\"OLD-1\",\"product_name\":\"Old\"}]", 1, "records"))
        db.localDao.putMutation(LocalMutation("legacy", scope.key, "Product", null, "original-key", "{\"product_name\":\"Queued\"}", 2))
        assertEquals(2, repo.records(scope, "Product").length())
        repo.sync(scope)
        assertEquals("original-key", remote.sent.single().first)
        assertNull(remote.operations.single().localId)
    }

    @Test fun separateDraftsAndScopesRemainIsolated() = runBlocking {
        db.localDao.putDraft(LocalDraft("draft-a", scope.key, "Product", null, "{}", 1, 1))
        db.localDao.putDraft(LocalDraft("draft-b", scope.key, "Product", null, "{}", 1, 1))
        repo.save(scope, "Product", null, JSONObject().put("product_name", "Milk"), "new-key", null, "draft-a")
        assertNull(db.localDao.getDraft(scope.key, "draft-a"))
        assertNotNull(db.localDao.getDraft(scope.key, "draft-b"))
        assertEquals(0, repo.records(scope.copy(userId = "other-user"), "Product").length())
    }

    @Test fun backoffSurvivesAnotherSyncAttemptWithoutSendingEarly() = runBlocking {
        remote.loseResponseOnce = true
        save("Milk")
        repo.sync(scope)
        val second = repo.sync(scope)
        assertEquals(0, second.acknowledged)
        assertNotNull(second.retryAt)
        assertEquals(1, remote.calls)
    }

    @Test fun rejectedRecordCanBeCorrectedWithoutBlockingItsNewOperation() = runBlocking {
        remote.rejectName = "Invalid"
        val id = save("Invalid")
        repo.sync(scope)
        val rejected = db.localDao.queuedMutations(scope.key).single()
        repo.correct(scope, rejected.id, JSONObject().put("product_name", "Corrected"), null)
        assertEquals("discarded", db.localDao.mutation(scope.key, rejected.id)!!.status)
        assertEquals(1, repo.sync(scope).acknowledged)
        assertEquals("Corrected", repo.record(scope, "Product", id)!!.getString("product_name"))
    }

    @Test fun pendingPosSaleAppearsInNormalSaleRecordsAndMapsOnSync() = runBlocking {
        val context = JSONObject().put("reference", "TEST-SALE-1").put("total", "24.00")
        val request = JSONObject().put("view", "pos").put("component", "payments").put("context", context)
        repo.saveAction(scope, "checkout", request, "pos-sale:TEST-SALE-1", "Sale", context)
        assertEquals("TEST-SALE-1", repo.records(scope, "Sale").getJSONObject(0).getString("reference"))
        repo.sync(scope)
        assertEquals(1, repo.records(scope, "Sale").length())
        assertEquals("SERVER-1", repo.records(scope, "Sale").getJSONObject(0).getString("name"))
        assertTrue(db.localDao.queuedMutations(scope.key).isEmpty())
    }

    @Test fun childTableSerializerPreservesTypedRows() {
        val fields = listOf(FieldSchema("title"), FieldSchema("items", fieldtype = "Table", options = "Sale Item"))
        val child = listOf(FieldSchema("product", fieldtype = "Link"), FieldSchema("quantity", fieldtype = "Int"), FieldSchema("rate", fieldtype = "Currency"))
        val result = FormPayloadSerializer.serialize(fields, mapOf("title" to "Sale", "items" to listOf(mapOf("product" to "P-1", "quantity" to "2", "rate" to "12.50"))), mapOf("Sale Item" to child))
        assertEquals(2, result.getJSONArray("items").getJSONObject(0).getInt("quantity"))
        assertEquals("12.50", result.getJSONArray("items").getJSONObject(0).getString("rate"))
        assertEquals("P-1", result.getJSONArray("items").getJSONObject(0).getString("product"))
    }

    @Test fun openingStockIsSentOnlyWhenCreatingProduct() {
        val fields = listOf(FieldSchema("product_name"), FieldSchema("stock_qty", fieldtype = "Int", readOnly = true, editableOnCreate = true))
        val values = mapOf<String, Any?>("product_name" to "New", "stock_qty" to "5")
        assertEquals(5, FormPayloadSerializer.serialize(fields, values, isNew = true).getInt("stock_qty"))
        assertFalse(FormPayloadSerializer.serialize(fields, values).has("stock_qty"))
    }

    @Test fun newOfflineProductCanBeSoldAndDependentSaleResolvesItsIdentity() = runBlocking {
        val product = repo.save(scope, "Product", null,
            JSONObject().put("product_name", "New milk").put("stock_qty", 5), "new-product", "New milk")
        val cart = JSONArray().put(JSONObject().put("product", product).put("quantity", 2))
        val sale = JSONObject().put("reference", "S-NEW").put("items", cart)
        val request = JSONObject().put("context", JSONObject().put("items", cart))
        val effect = JSONObject().put("doctype", "Product").put("quantity_field", "stock_qty")
            .put("lines", JSONArray().put(JSONObject().put("name", product).put("quantity", 2)))
        repo.saveAction(scope, "complete", request, "new-sale", "Sale", sale, effect)
        assertEquals(3, repo.record(scope, "Product", product)!!.getInt("stock_qty"))
        assertEquals(1, repo.records(scope, "Sale").length())
        remote.rows = listOf(JSONObject().put("name", "SERVER-1").put("product_name", "New milk")
            .put("stock_qty", 3).put("_version", "v2"))
        assertEquals(2, repo.sync(scope).acknowledged)
        assertEquals("SERVER-1", remote.bodies.last().getJSONObject("context").getJSONArray("items").getJSONObject(0).getString("product"))
        assertEquals("SERVER-1", repo.records(scope, "Sale").getJSONObject(0).getJSONArray("items").getJSONObject(0).getString("product"))
        assertEquals(3, repo.record(scope, "Product", product)!!.getInt("stock_qty"))
        assertTrue(db.localDao.queuedMutations(scope.key).isEmpty())
    }

    private suspend fun save(name: String) = repo.save(scope, "Product", null, JSONObject().put("product_name", name), "key-$name", name)

    @Test fun interruptedBootstrapKeepsOldListAndResumesConsistentSnapshot() = runBlocking {
        remote.rows = listOf(JSONObject().put("name", "OLD").put("product_name", "Old"))
        repo.refresh(scope, "Product")
        remote.bootstrapRows = listOf(
            JSONObject().put("name", "NEW-1").put("product_name", "New one"),
            JSONObject().put("name", "NEW-2").put("product_name", "New two"),
        )
        remote.interruptSecondBootstrapPage = true
        try { repo.bootstrap(scope, mapOf("Product" to "Product")); fail("Expected interrupted download") }
        catch (_: KoraError) { }
        assertNotNull(repo.record(scope, "Product", "OLD"))
        assertNotNull(db.localDao.getEntry(scope.key, "offline-bootstrap-progress"))
        repo.bootstrap(scope, mapOf("Product" to "Product"))
        assertNull(repo.record(scope, "Product", "OLD"))
        assertEquals(2, repo.records(scope, "Product").length())
        assertTrue(repo.hasPullCursor(scope))
    }

    @Test fun pullAppliesChangeAndDeletionWithCursorAndPreservesPendingConflict() = runBlocking {
        remote.bootstrapRows = listOf(JSONObject().put("name", "P-1").put("product_name", "Original"))
        repo.bootstrap(scope, mapOf("Product" to "Product"))
        repo.save(scope, "Product", "P-1", JSONObject().put("product_name", "Local edit"), "edit-1", "Local edit")
        remote.nextPull = JSONObject().put("changes", JSONArray().put(JSONObject().put("doctype", "Product")
            .put("record", JSONObject().put("name", "P-2").put("product_name", "Remote"))))
            .put("tombstones", JSONArray().put(JSONObject().put("doctype", "Product").put("name", "P-1")))
            .put("next_cursor", "cursor-2").put("has_more", false)
        repo.pull(scope)
        assertEquals("Remote", repo.record(scope, "Product", "P-2")!!.getString("product_name"))
        assertEquals("Local edit", repo.record(scope, "Product", "P-1")!!.getString("product_name"))
        assertEquals("conflict", repo.record(scope, "Product", "P-1")!!.getString("_sync_status"))
        assertEquals("cursor-2", db.localDao.getEntry(scope.key, "offline-pull-cursor")!!.encryptedPayload)
    }

    @Test fun invalidPullPageRollsBackRecordAndCursorTogether() = runBlocking {
        remote.bootstrapRows = emptyList()
        repo.bootstrap(scope, mapOf("Product" to "Product"))
        remote.nextPull = JSONObject().put("changes", JSONArray().put(JSONObject().put("doctype", "Product")
            .put("record", JSONObject().put("name", "P-2").put("product_name", "Remote")))
            .put(JSONObject().put("doctype", "Product")))
            .put("tombstones", JSONArray()).put("next_cursor", "cursor-2").put("has_more", false)
        try { repo.pull(scope); fail("Expected invalid page") } catch (_: KoraError) { }
        assertNull(repo.record(scope, "Product", "P-2"))
        assertEquals("cursor-1", db.localDao.getEntry(scope.key, "offline-pull-cursor")!!.encryptedPayload)
    }

    @Test fun changedConfigurationCannotCompleteOfflineDownload() = runBlocking {
        remote.bootstrapGeneration = 2
        remote.bootstrapRows = listOf(JSONObject().put("name", "P-1").put("product_name", "New"))
        try { repo.bootstrap(scope, mapOf("Product" to "Product"), expectedGeneration = 1); fail("Expected generation gate") }
        catch (error: KoraError) { assertEquals("offline.generation_changed", error.code) }
        assertFalse(repo.hasPullCursor(scope))
        assertNull(repo.record(scope, "Product", "P-1"))
    }

    private fun stockEffect(quantity: Int) = JSONObject().put("doctype", "Product").put("quantity_field", "stock_qty")
        .put("lines", JSONArray().put(JSONObject().put("name", "P-1").put("quantity", quantity)))

    @Test fun multipleOfflineSalesReserveStockThenReconcileAcceptedQuantity() = runBlocking {
        remote.rows = listOf(JSONObject().put("name", "P-1").put("product_name", "Milk").put("stock_qty", 10).put("_version", "v1"))
        repo.refresh(scope, "Product")
        repo.saveAction(scope, "complete", JSONObject().put("context", JSONObject().put("reference", "S-1")), "sale-1", "Sale", JSONObject().put("reference", "S-1"), stockEffect(3))
        repo.saveAction(scope, "complete", JSONObject().put("context", JSONObject().put("reference", "S-2")), "sale-2", "Sale", JSONObject().put("reference", "S-2"), stockEffect(4))
        assertEquals(3, repo.record(scope, "Product", "P-1")!!.getInt("stock_qty"))
        assertEquals(2, repo.records(scope, "Sale").length())
        remote.rows = listOf(JSONObject().put("name", "P-1").put("product_name", "Milk").put("stock_qty", 3).put("_version", "v2"))
        assertEquals(2, repo.sync(scope).acknowledged)
        assertEquals(3, repo.record(scope, "Product", "P-1")!!.getInt("stock_qty"))
    }

    @Test fun rejectedSaleRestoresProvisionalStockAndInsufficientSaleDoesNotCommit() = runBlocking {
        remote.rows = listOf(JSONObject().put("name", "P-1").put("product_name", "Milk").put("stock_qty", 5).put("_version", "v1"))
        repo.refresh(scope, "Product")
        try { repo.saveAction(scope, "complete", JSONObject(), "too-many", "Sale", JSONObject().put("reference", "S-0"), stockEffect(6)); fail("Expected unavailable stock") }
        catch (error: KoraError) { assertEquals("stock_unavailable", error.code) }
        assertEquals(0, repo.records(scope, "Sale").length())
        repo.saveAction(scope, "complete", JSONObject(), "sale-1", "Sale", JSONObject().put("reference", "S-1"), stockEffect(3))
        assertEquals(2, repo.record(scope, "Product", "P-1")!!.getInt("stock_qty"))
        remote.rejectAction = true
        repo.sync(scope)
        assertEquals(5, repo.record(scope, "Product", "P-1")!!.getInt("stock_qty"))
        assertEquals("rejected", repo.records(scope, "Sale").getJSONObject(0).getString("_sync_status"))
    }

    @Test fun signedAdjustmentProjectsOfflineAndReconcilesOrRestoresStock() = runBlocking {
        remote.rows = listOf(JSONObject().put("name", "P-1").put("stock_qty", 5).put("_version", "v1"))
        repo.refresh(scope, "Product")
        repo.saveAction(scope, "apply", JSONObject(), "adjust-1", "Stock Adjustment",
            JSONObject().put("product", "P-1").put("delta", 3).put("reason", "Counted delivery"), stockEffect(-3))
        assertEquals(8, repo.record(scope, "Product", "P-1")!!.getInt("stock_qty"))
        remote.rows = listOf(JSONObject().put("name", "P-1").put("stock_qty", 8).put("_version", "v2"))
        assertEquals(1, repo.sync(scope).acknowledged)
        assertEquals(8, repo.record(scope, "Product", "P-1")!!.getInt("stock_qty"))

        repo.saveAction(scope, "apply", JSONObject(), "adjust-2", "Stock Adjustment",
            JSONObject().put("product", "P-1").put("delta", -4).put("reason", "Damaged"), stockEffect(4))
        assertEquals(4, repo.record(scope, "Product", "P-1")!!.getInt("stock_qty"))
        remote.rejectAction = true
        repo.sync(scope)
        assertEquals(8, repo.record(scope, "Product", "P-1")!!.getInt("stock_qty"))
        val adjustments = repo.records(scope, "Stock Adjustment")
        assertTrue((0 until adjustments.length()).any { adjustments.getJSONObject(it).optString("_sync_status") == "rejected" })
    }

    private class FakeRemote : RecordRemoteDataSource {
        override suspend fun capabilities(scope: Scope): JSONObject = JSONObject().put("generation", 1)
        var bootstrapRows = emptyList<JSONObject>()
        var bootstrapGeneration = 1
        var interruptSecondBootstrapPage = false
        var nextPull = JSONObject().put("changes", JSONArray()).put("tombstones", JSONArray()).put("next_cursor", "cursor-1").put("has_more", false)
        override suspend fun bootstrap(scope: Scope, doctypes: List<String>, snapshotToken: String?, pageToken: String?): JSONObject {
            val offset = pageToken?.toIntOrNull() ?: 0
            if (offset > 0 && interruptSecondBootstrapPage) { interruptSecondBootstrapPage = false; throw KoraError("network_error", "Interrupted", retryable = true) }
            val part = bootstrapRows.drop(offset).take(1)
            val more = offset + part.size < bootstrapRows.size
            return JSONObject().put("snapshot_token", "snapshot-1").put("generation", bootstrapGeneration).put("records", JSONArray(part.map { JSONObject().put("doctype", "Product").put("record", it) }))
                .put("complete", !more).put("next_page_token", if (more) "${offset + part.size}" else "")
                .put("pull_cursor", if (more) "" else "cursor-1")
        }
        override suspend fun pull(scope: Scope, cursor: String): JSONObject = nextPull
        var calls = 0
        var rejectName: String? = null
        var rejectAction = false
        var cancel = false
        var loseResponseOnce = false
        var duringSend: (suspend () -> Unit)? = null
        var rows = emptyList<JSONObject>()
        val offsets = mutableListOf<Int>()
        val effects = linkedMapOf<String, JSONObject>()
        val sent = mutableListOf<Pair<String, String?>>()
        val bodies = mutableListOf<JSONObject>()
        val operations = mutableListOf<LocalMutation>()
        override suspend fun list(scope: Scope, doctype: String, offset: Int, limit: Int): JSONArray {
            offsets += offset
            return JSONArray(rows.drop(offset).take(limit))
        }
        override suspend fun get(scope: Scope, doctype: String, name: String) = rows.first { it.optString("name") == name }
        override suspend fun send(scope: Scope, operation: LocalMutation, serverId: String?, payload: JSONObject): JSONObject {
            calls++; sent += operation.idempotencyKey to serverId; bodies += payload; operations += operation
            if (cancel) throw CancellationException("Process interrupted")
            if (rejectAction && operation.doctype.startsWith("@view-action/")) throw KoraError("invalid", "Rejected sale", 400)
            if (payload.optString("product_name") == rejectName) throw KoraError("invalid", "Invalid name", 422)
            duringSend?.also { duringSend = null }?.invoke()
            val result = effects.getOrPut(operation.idempotencyKey) { JSONObject(payload.toString()).put("name", serverId ?: "SERVER-${effects.size + 1}").put("modified", "2026-10-23T12:00:00Z") }
            if (loseResponseOnce) { loseResponseOnce = false; throw KoraError("network_error", "Lost response", retryable = true) }
            return result
        }
    }
}
