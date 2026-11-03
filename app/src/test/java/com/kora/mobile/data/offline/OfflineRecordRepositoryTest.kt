package com.kora.mobile.data.offline

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.kora.mobile.core.contracts.EngineContractDecoder
import com.kora.mobile.core.contracts.EnginePublishedOperation
import com.kora.mobile.core.contracts.EnginePublishedQuery
import com.kora.mobile.core.contracts.EngineSchemaDocument
import com.kora.mobile.core.contracts.EngineSchemaRef
import com.kora.mobile.core.contracts.decodeApplicationCatalog
import com.kora.mobile.core.database.KoraDatabase
import com.kora.mobile.core.security.PayloadCipher
import com.kora.mobile.domain.model.BusinessScope
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.URI
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference

@RunWith(RobolectricTestRunner::class)
class OfflineRecordRepositoryTest {
    private class Cipher : PayloadCipher {
        override fun encrypt(value: String) = "enc:$value"
        override fun decrypt(encoded: String) = encoded.removePrefix("enc:").takeIf { encoded.startsWith("enc:") }
    }

    @Test fun intentIsDurableEncryptedAndIsolatedByActorScope() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, KoraDatabase::class.java).allowMainThreadQueries().build()
        try {
            val scope = Scope("i", "https://example.test", "site", "alice", BusinessScope("tenant-a"))
            val catalog = catalog(4)
            val repo = OfflineRecordRepository(db, Cipher(), RestRecordDataSource(com.kora.mobile.core.network.KoraHttpClient(object : com.kora.mobile.core.security.SessionSecretStore {
                override fun put(key: String, value: String) = Unit
                override fun get(key: String): String? = null
                override fun remove(key: String) = Unit
            })), EngineContractDecoder.fromAssets(context))
            val operation = catalog.operations.single()
            val id = repo.enqueue(scope, catalog, operation, JSONObject().put("amount", 3), "stable-key-4")
            val saved = db.localDao.pendingIntents(scope.key).single()
            assertEquals(id, saved.localOperationId)
            assertEquals("stable-key-4", saved.idempotencyKey)
            assertTrue(saved.encryptedInput.startsWith("enc:"))
            assertTrue(db.localDao.pendingIntents(scope.copy(userId = "bob").key).isEmpty())
            assertTrue(db.localDao.pendingIntents(scope.copy(businessScope = BusinessScope("tenant-a", "legal_entity", "entity-b")).key).isEmpty())
            assertTrue(db.localDao.facts(scope.key, "sales.orders").isEmpty())
            db.localDao.putFact(com.kora.mobile.core.database.OfflineFactEntity(scope.key, "sales.orders", "row-1", "enc:{\"id\":\"row-1\"}", false, 1))
            db.localDao.putFact(com.kora.mobile.core.database.OfflineFactEntity(scope.key, "sales.orders", "row-1", null, true, 2))
            val tombstone = db.localDao.facts(scope.key, "sales.orders")
            assertTrue(tombstone.isEmpty())
            val storedTombstone = db.localDao.observeFacts(scope.key, "sales.orders").first().single()
            assertTrue(storedTombstone.deleted)
            assertNull(storedTombstone.encryptedData)
            db.openHelper.readableDatabase.query("SELECT name FROM sqlite_master WHERE type='table'").use { cursor ->
                val names = buildSet { while (cursor.moveToNext()) add(cursor.getString(0)) }
                assertFalse(names.contains("local_outbox"))
                assertFalse(names.contains("local_records"))
                assertTrue(names.contains("offline_intents"))
            }
        } finally { db.close() }
    }

    @Test fun unavailableOrInvalidOperationCannotBecomeIntent() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val db = Room.inMemoryDatabaseBuilder(context, KoraDatabase::class.java).allowMainThreadQueries().build()
        try {
            val scope = Scope("i", "https://example.test", "site", "alice", BusinessScope("tenant-a"))
            val repo = OfflineRecordRepository(db, Cipher(), RestRecordDataSource(com.kora.mobile.core.network.KoraHttpClient(object : com.kora.mobile.core.security.SessionSecretStore {
                override fun put(key: String, value: String) = Unit
                override fun get(key: String): String? = null
                override fun remove(key: String) = Unit
            })), EngineContractDecoder.fromAssets(context))
            val catalog = catalog(4)
            assertTrue(runCatching { repo.enqueue(scope, catalog, catalog.operations.single(), JSONObject().put("wrong", 1), "bad-key") }.isFailure)
            assertEquals(0, db.localDao.pendingIntents(scope.key).size)
            val onlineOnly = catalog.operations.single().copy(offlineEligible = false)
            assertTrue(runCatching { repo.enqueue(scope, catalog, onlineOnly, JSONObject().put("amount", 1), "bad-key") }.isFailure)
        } finally { db.close() }
    }

    @Test fun lostResponseAfterSendIsRecoveredByLookupAfterRepositoryRestart() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sends = AtomicInteger()
        val lookups = AtomicInteger()
        val sentIntent = AtomicReference<JSONObject>()
        val lookup = AtomicReference<JSONObject>()
        val server = ServerSocket().apply { bind(InetSocketAddress("127.0.0.1", 0)) }
        val thread = Thread {
            while (!server.isClosed) runCatching {
                server.accept().use { socket ->
                    val request = readRequest(socket.getInputStream())
                    when (URI(request.first).path.removePrefix("/s/site")) {
                        "/api/v2/offline/capabilities" -> respond(socket, capabilitiesJson())
                        "/api/v2/offline/intents" -> {
                            sends.incrementAndGet()
                            sentIntent.set(JSONObject(request.second))
                            // Simulate Engine applying the operation and losing the response.
                        }
                        "/api/v2/module-executions/lookup" -> {
                            lookups.incrementAndGet()
                            lookup.set(JSONObject(request.second))
                            respond(socket, receiptJson())
                        }
                        else -> respond(socket, JSONObject().put("error", JSONObject().put("code", "not_found")), 404)
                    }
                }
            }
        }.apply { isDaemon = true; start() }
        val db = Room.inMemoryDatabaseBuilder(context, KoraDatabase::class.java).allowMainThreadQueries().build()
        try {
            val scope = Scope("i", "http://127.0.0.1:${server.localPort}", "site", "alice", BusinessScope("tenant-a"))
            val catalog = catalog(4)
            val repo1 = repository(context, db)
            val id = repo1.enqueue(scope, catalog, catalog.operations.single(), JSONObject().put("amount", 3), "same-key-after-crash")
            db.localDao.intent(scope.key, id)?.let { db.localDao.putIntent(it.copy(status = "sent")) }

            val repoAfterProcessDeath = repository(context, db)
            val recovered = repoAfterProcessDeath.sync(scope, catalog)
            assertNull(recovered.error)
            assertEquals(0, sends.get())
            assertEquals(1, lookups.get())
            assertEquals("same-key-after-crash", lookup.get().getString("idempotency_key"))
            assertEquals("sales.orders~orders~create", lookup.get().getString("operation_ref"))
            assertEquals("applied", db.localDao.intent(scope.key, id)?.status)
        } finally {
            db.close()
            server.close()
            thread.join(1000)
        }
    }

    @Test fun missingReceiptIsUncertainAndRetryUsesSameLocalIdAndKey() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sends = AtomicInteger()
        val requestBodies = mutableListOf<JSONObject>()
        val server = ServerSocket().apply { bind(InetSocketAddress("127.0.0.1", 0)) }
        val thread = Thread {
            while (!server.isClosed) runCatching {
                server.accept().use { socket ->
                    val request = readRequest(socket.getInputStream())
                    when (URI(request.first).path.removePrefix("/s/site")) {
                        "/api/v2/offline/capabilities" -> respond(socket, capabilitiesJson())
                        "/api/v2/module-executions/lookup" -> respondError(socket, "module.operation_receipt_not_found", 404)
                        "/api/v2/offline/intents" -> {
                            val body = JSONObject(request.second)
                            requestBodies += body
                            if (sends.incrementAndGet() == 1) respond(socket, JSONObject().put("receipts", org.json.JSONArray()))
                            else {
                                val intent = body.getJSONArray("intents").getJSONObject(0)
                                respond(socket, JSONObject().put("receipts", org.json.JSONArray().put(JSONObject()
                                    .put("local_operation_id", intent.getString("local_operation_id")).put("receipt", receiptJson()))))
                            }
                        }
                        else -> respond(socket, JSONObject().put("error", JSONObject().put("code", "not_found")), 404)
                    }
                }
            }
        }.apply { isDaemon = true; start() }
        val db = Room.inMemoryDatabaseBuilder(context, KoraDatabase::class.java).allowMainThreadQueries().build()
        try {
            val scope = Scope("i", "http://127.0.0.1:${server.localPort}", "site", "alice", BusinessScope("tenant-a"))
            val catalog = catalog(4)
            val repo = repository(context, db)
            val id = repo.enqueue(scope, catalog, catalog.operations.single(), JSONObject().put("amount", 2), "stable-partial-key")
            assertNotNull(repo.sync(scope, catalog).error)
            assertEquals("outcome_unknown", db.localDao.intent(scope.key, id)?.status)
            val afterRestart = repository(context, db)
            assertNull(afterRestart.sync(scope, catalog).error)
            assertEquals(2, sends.get())
            assertEquals(id, requestBodies[0].getJSONArray("intents").getJSONObject(0).getString("local_operation_id"))
            assertEquals(id, requestBodies[1].getJSONArray("intents").getJSONObject(0).getString("local_operation_id"))
            assertEquals("stable-partial-key", requestBodies[0].getJSONArray("intents").getJSONObject(0).getString("idempotency_key"))
            assertEquals("stable-partial-key", requestBodies[1].getJSONArray("intents").getJSONObject(0).getString("idempotency_key"))
            assertEquals("applied", db.localDao.intent(scope.key, id)?.status)
        } finally {
            db.close()
            server.close()
            thread.join(1000)
        }
    }

    @Test fun bootstrapAndPagedPullAtomicallyInstallFactsAndTombstones() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val secondPullRequest = AtomicReference<JSONObject>()
        val server = ServerSocket().apply { bind(InetSocketAddress("127.0.0.1", 0)) }
        val thread = Thread {
            while (!server.isClosed) runCatching {
                server.accept().use { socket ->
                    val request = readRequest(socket.getInputStream())
                    val path = URI(request.first).path.removePrefix("/s/site")
                    val body = request.second.takeIf(String::isNotBlank)?.let(::JSONObject) ?: JSONObject()
                    when (path) {
                        "/api/v2/offline/capabilities" -> respond(socket, capabilitiesJson())
                        "/api/v2/offline/bootstrap" -> respond(socket, snapshotJson(body.getJSONObject("source")))
                        "/api/v2/offline/pull" -> {
                            if (body.has("page_cursor")) {
                                secondPullRequest.set(body)
                                respond(socket, pullJson(body.getJSONObject("source"), listOf(change("row-2", false, JSONObject().put("id", "row-2"))), false, null, "cursor-through"))
                            } else respond(socket, pullJson(body.getJSONObject("source"), listOf(change("row-1", true, null)), true, "feed-page-2", "cursor-through"))
                        }
                        else -> respondError(socket, "not_found", 404)
                    }
                }
            }
        }.apply { isDaemon = true; start() }
        val db = Room.inMemoryDatabaseBuilder(context, KoraDatabase::class.java).allowMainThreadQueries().build()
        try {
            val scope = Scope("i", "http://127.0.0.1:${server.localPort}", "site", "alice", BusinessScope("tenant-a"))
            val queryCatalog = feedCatalog()
            val query = queryCatalog.queries.single()
            val input = JSONObject()
            val repo = repository(context, db)
            val snapshot = repo.bootstrap(scope, queryCatalog, query, input)
            assertEquals("cursor-1", snapshot.getString("replay_position"))
            assertEquals(1, db.localDao.facts(scope.key, "sales.orders").size)
            val saved = repo.savedCheckpoint(scope, queryCatalog, query, input) ?: error("snapshot checkpoint not saved")
            repo.pull(scope, queryCatalog, query, input, saved)
            assertEquals("feed-page-2", secondPullRequest.get().getString("page_cursor"))
            assertEquals("cursor-through", secondPullRequest.get().getString("through_cursor"))
            assertEquals(listOf("row-2"), db.localDao.facts(scope.key, "sales.orders").map { it.factKey })
            val rows = db.localDao.observeFacts(scope.key, "sales.orders").first().associateBy { it.factKey }
            assertTrue(rows.getValue("row-1").deleted)
            assertFalse(rows.getValue("row-2").deleted)
            assertEquals("cursor-through", repo.savedCheckpoint(scope, queryCatalog, query, input)?.getString("replay_position"))
            assertFalse(repo.savedCheckpoint(scope, queryCatalog, query, input)?.has("page_cursor") == true)
        } finally {
            db.close()
            server.close()
            thread.join(1000)
        }
    }

    @Test fun snapshotIdentityChangeMidBootstrapKeepsPreviousSnapshot() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val bootstrapPages = AtomicInteger()
        val server = ServerSocket().apply { bind(InetSocketAddress("127.0.0.1", 0)) }
        val thread = Thread {
            while (!server.isClosed) runCatching {
                server.accept().use { socket ->
                    val request = readRequest(socket.getInputStream())
                    val path = URI(request.first).path.removePrefix("/s/site")
                    val body = request.second.takeIf(String::isNotBlank)?.let(::JSONObject) ?: JSONObject()
                    when (path) {
                        "/api/v2/offline/capabilities" -> respond(socket, capabilitiesJson())
                        "/api/v2/offline/bootstrap" -> {
                            val page = bootstrapPages.incrementAndGet()
                            val source = sourceJson(body.getJSONObject("source"))
                            val snapshot = snapshotJson(source).put("snapshot_token", if (page == 1) "snapshot-a" else "snapshot-b")
                                .put("complete", page != 1)
                            if (page == 1) snapshot.put("next_page_cursor", "page-2")
                            else snapshot.getJSONArray("rows").put(JSONObject().put("id", "row-2"))
                            respond(socket, snapshot)
                        }
                        else -> respondError(socket, "not_found", 404)
                    }
                }
            }
        }.apply { isDaemon = true; start() }
        val db = Room.inMemoryDatabaseBuilder(context, KoraDatabase::class.java).allowMainThreadQueries().build()
        try {
            val scope = Scope("i", "http://127.0.0.1:${server.localPort}", "site", "alice", BusinessScope("tenant-a"))
            val queryCatalog = feedCatalog()
            val query = queryCatalog.queries.single()
            val repo = repository(context, db)
            repo.replaceSnapshot(scope, query.ownerModuleId, listOf("previous-row" to JSONObject().put("id", "previous-row")), complete = true)

            val result = runCatching { repo.bootstrap(scope, queryCatalog, query, JSONObject()) }
            assertTrue("snapshot token changed between pages", result.isFailure)
            assertEquals(2, bootstrapPages.get())
            assertEquals(listOf("previous-row"), db.localDao.facts(scope.key, query.ownerModuleId).map { it.factKey })
            assertNull(repo.savedCheckpoint(scope, queryCatalog, query, JSONObject()))
        } finally {
            db.close()
            server.close()
            thread.join(1000)
        }
    }

    @Test fun revokedOperationOrSchemaBlocksSavedIntentWithoutDispatch() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val sends = AtomicInteger()
        val server = ServerSocket().apply { bind(InetSocketAddress("127.0.0.1", 0)) }
        val thread = Thread {
            while (!server.isClosed) runCatching {
                server.accept().use { socket ->
                    val request = readRequest(socket.getInputStream())
                    when (URI(request.first).path.removePrefix("/s/site")) {
                        "/api/v2/offline/capabilities" -> respond(socket, capabilitiesJson())
                        "/api/v2/offline/intents" -> {
                            sends.incrementAndGet()
                            respond(socket, JSONObject().put("receipts", org.json.JSONArray()))
                        }
                        else -> respondError(socket, "not_found", 404)
                    }
                }
            }
        }.apply { isDaemon = true; start() }
        val db = Room.inMemoryDatabaseBuilder(context, KoraDatabase::class.java).allowMainThreadQueries().build()
        try {
            val scope = Scope("i", "http://127.0.0.1:${server.localPort}", "site", "alice", BusinessScope("tenant-a"))
            val repo = repository(context, db)
            val originalCatalog = catalog(4)
            val operation = originalCatalog.operations.single()
            val intentId = repo.enqueue(scope, originalCatalog, operation, JSONObject().put("amount", 3), "stable-revocation-key")

            val revokedOperationCatalog = originalCatalog.copy(operations = listOf(operation.copy(availability = "draining")))
            val revokedResult = repo.sync(scope, revokedOperationCatalog)
            assertNotNull(revokedResult.error)
            assertEquals("blocked", db.localDao.intent(scope.key, intentId)?.status)
            assertEquals(0, sends.get())

            val schemaIntentId = repo.enqueue(scope, originalCatalog, operation, JSONObject().put("amount", 4), "stable-schema-revocation-key")
            val revokedSchemaCatalog = originalCatalog.copy(schemas = originalCatalog.schemas.filterNot { it.ref == operation.inputSchema })
            val schemaResult = repo.sync(scope, revokedSchemaCatalog)
            assertNotNull(schemaResult.error)
            assertEquals("blocked", db.localDao.intent(scope.key, schemaIntentId)?.status)
            assertEquals(0, sends.get())
        } finally {
            db.close()
            server.close()
            thread.join(1000)
        }
    }

    private fun repository(context: Context, db: KoraDatabase) = OfflineRecordRepository(db, Cipher(), RestRecordDataSource(
        com.kora.mobile.core.network.KoraHttpClient(object : com.kora.mobile.core.security.SessionSecretStore {
            override fun put(key: String, value: String) = Unit
            override fun get(key: String): String? = null
            override fun remove(key: String) = Unit
        })), EngineContractDecoder.fromAssets(context))

    private fun capabilitiesJson() = JSONObject().put("protocol_version", 2).put("catalog_revision", 4).put("max_batch_size", 20).put("max_page_size", 100).put("snapshot_lifetime_seconds", 86400)
    private fun receiptJson() = JSONObject().put("execution_id", "exec-4").put("operation_ref", "sales.orders~orders~create")
        .put("business_scope", JSONObject().put("tenant_id", "tenant-a").put("kind", "tenant"))
        .put("status", "completed").put("output_schema", JSONObject().put("id", "orders.output").put("version", 1))
        .put("output", JSONObject()).put("revision", "4")

    private fun feedCatalog(): com.kora.mobile.core.contracts.EngineApplicationCatalog {
        val inputRef = EngineSchemaRef("feed.input", 1)
        val outputRef = EngineSchemaRef("feed.output", 1)
        val rowSchema = JSONObject().put("type", "object").put("required", org.json.JSONArray().put("id"))
            .put("properties", JSONObject().put("id", JSONObject().put("type", "string")))
        val query = EnginePublishedQuery("sales.orders", "list", "/api/v1/modules/sales.orders/queries/list", inputRef, outputRef,
            "tenant_shared", 10, true, listOf("id"), JSONObject(), "id")
        return catalog(4).copy(queries = listOf(query), schemas = listOf(
            EngineSchemaDocument(inputRef, JSONObject().put("type", "object")),
            EngineSchemaDocument(outputRef, JSONObject().put("type", "array").put("items", rowSchema)),
        ))
    }

    private fun sourceJson(source: JSONObject) = JSONObject().put("owner_module_id", source.getString("owner_module_id"))
        .put("query_name", source.getString("query_name")).put("input_schema", source.getJSONObject("input_schema")).put("input", source.get("input"))

    private fun snapshotJson(source: JSONObject) = JSONObject().put("source", sourceJson(source)).put("input_hash", sha256("{}"))
        .put("business_scope", JSONObject().put("tenant_id", "tenant-a").put("kind", "tenant")).put("subject_id", "alice")
        .put("schema_revision", JSONObject().put("id", "feed.output").put("version", 1)).put("catalog_revision", 4)
        .put("fact_key_field", "id")
        .put("snapshot_token", "snapshot-1").put("replay_position", "cursor-1").put("rows", org.json.JSONArray().put(JSONObject().put("id", "row-1"))).put("complete", true)

    private fun pullJson(source: JSONObject, changes: List<JSONObject>, hasMore: Boolean, next: String?, through: String) = JSONObject()
        .put("source", sourceJson(source)).put("input_hash", sha256("{}"))
        .put("business_scope", JSONObject().put("tenant_id", "tenant-a").put("kind", "tenant")).put("subject_id", "alice")
        .put("schema_revision", JSONObject().put("id", "feed.output").put("version", 1)).put("catalog_revision", 4)
        .put("snapshot_token", "snapshot-1").put("replay_position", "cursor-1").put("through_cursor", through)
        .put("changes", org.json.JSONArray(changes)).put("has_more", hasMore).also { if (next != null) it.put("next_page_cursor", next) }

    private fun change(key: String, deleted: Boolean, data: JSONObject?) = JSONObject().put("fact_key", key).put("deleted", deleted).also { if (data != null) it.put("data", data) }
    private fun sha256(value: String) = java.security.MessageDigest.getInstance("SHA-256").digest(value.toByteArray()).joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun respond(socket: java.net.Socket, value: JSONObject, status: Int = 200) {
        val bytes = JSONObject().put("data", value).toString().toByteArray()
        val text = if (status == 200) "OK" else "Not Found"
        socket.getOutputStream().apply {
            write("HTTP/1.1 $status $text\r\nContent-Type: application/json\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n".toByteArray())
            write(bytes)
            flush()
        }
    }

    private fun respondError(socket: java.net.Socket, code: String, status: Int) {
        val bytes = JSONObject().put("error", JSONObject().put("code", code).put("message", "Not found")).toString().toByteArray()
        socket.getOutputStream().apply {
            write("HTTP/1.1 $status Not Found\r\nContent-Type: application/json\r\nContent-Length: ${bytes.size}\r\nConnection: close\r\n\r\n".toByteArray())
            write(bytes)
            flush()
        }
    }

    private fun readRequest(input: java.io.InputStream): Pair<String, String> {
        val stream = BufferedInputStream(input)
        val headers = ByteArrayOutputStream()
        val marker = byteArrayOf(13, 10, 13, 10)
        var matched = 0
        while (matched < marker.size) {
            val byte = stream.read()
            if (byte < 0) error("incomplete request")
            headers.write(byte)
            matched = if (byte == marker[matched].toInt()) matched + 1 else if (byte == marker[0].toInt()) 1 else 0
        }
        val lines = headers.toString(Charsets.ISO_8859_1.name()).split("\r\n")
        val length = lines.drop(1).firstOrNull { it.startsWith("content-length:", true) }?.substringAfter(':')?.trim()?.toIntOrNull() ?: 0
        val body = ByteArray(length)
        var offset = 0
        while (offset < body.size) {
            val count = stream.read(body, offset, body.size - offset)
            if (count < 0) error("incomplete body")
            offset += count
        }
        return lines.first().split(' ').getOrElse(1) { "" } to body.toString(Charsets.UTF_8)
    }

    private fun catalog(revision: Long) = EngineContractDecoder.fromAssets(ApplicationProvider.getApplicationContext())
        .decodeApplicationCatalog(JSONObject().put("revision", revision).put("business_scope", JSONObject().put("tenant_id", "tenant-a").put("kind", "tenant"))
            .put("modules", org.json.JSONArray().put(JSONObject().put("id", "sales.orders").put("version", "1.0.0").put("state", "active")))
            .put("published_queries", org.json.JSONArray()).put("published_projections", org.json.JSONArray())
            .put("published_operations", org.json.JSONArray().put(JSONObject().put("operation_ref", "sales.orders~orders~create").put("owner_module_id", "sales.orders")
                .put("input_schema", JSONObject().put("id", "orders.input").put("version", 1)).put("output_schema", JSONObject().put("id", "orders.output").put("version", 1))
                .put("availability", "available").put("offline_eligible", true).put("capability", "orders.write").put("operation", "create")
                .put("permissions", org.json.JSONArray()).put("presentation", JSONObject())))
            .put("schemas", org.json.JSONArray().put(JSONObject().put("ref", JSONObject().put("id", "orders.input").put("version", 1))
                .put("schema", JSONObject().put("type", "object").put("required", org.json.JSONArray().put("amount")).put("properties", JSONObject().put("amount", JSONObject().put("type", "integer")))))
                .put(JSONObject().put("ref", JSONObject().put("id", "orders.output").put("version", 1)).put("schema", JSONObject().put("type", "object"))))
            .put("allowed_entity_choices", org.json.JSONArray()).put("blockers", org.json.JSONArray()).toString())
}
