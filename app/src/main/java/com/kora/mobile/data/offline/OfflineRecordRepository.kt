package com.kora.mobile.data.offline

import androidx.room.withTransaction
import com.kora.mobile.core.contracts.*
import com.kora.mobile.core.database.KoraDatabase
import com.kora.mobile.core.database.OfflineFactEntity
import com.kora.mobile.core.database.OfflineIntentEntity
import com.kora.mobile.core.database.OfflineCheckpointEntity
import com.kora.mobile.core.security.PayloadCipher
import com.kora.mobile.domain.model.KoraError
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Durable E4 intents and module facts. Intents are never applied as local record mutations. */
class OfflineRecordRepository(
    private val db: KoraDatabase,
    private val cipher: PayloadCipher,
    private val remote: RestRecordDataSource,
    private val decoder: EngineContractDecoder,
    private val canSync: (Scope) -> Boolean = { true },
) {
    private val dao = db.localDao
    private val syncMutex = Mutex()
    private val lastCatalogs = java.util.concurrent.ConcurrentHashMap<String, EngineApplicationCatalog>()
    private fun encode(value: Any) = cipher.encrypt(value.toString())
    private fun decodeObject(value: String) = cipher.decrypt(value)?.let(::JSONObject)
        ?: throw IllegalStateException("Saved offline data could not be decrypted")

    fun rememberCatalog(scope: Scope, catalog: EngineApplicationCatalog) { lastCatalogs[scope.key] = catalog }

    suspend fun enqueue(scope: Scope, catalog: EngineApplicationCatalog, operation: EnginePublishedOperation, input: Any, idempotencyKey: String, expectedRevision: String? = null): String {
        require(catalog.businessScope == scope.businessScope) { "Catalog belongs to another actor scope" }
        require(catalog.operations.any { it == operation } && operation.availability == "available" && operation.offlineEligible) { "This published operation is not available for offline execution" }
        require(idempotencyKey.isNotBlank() && idempotencyKey.length <= 256) { "An idempotency key must contain 1–256 characters" }
        require(expectedRevision == null || expectedRevision.matches(Regex("^(0|[1-9][0-9]{0,19})$"))) { "Expected revision must be a canonical decimal revision" }
        val schema = catalog.schemas.singleOrNull { it.ref == operation.inputSchema }?.schema
            ?: throw IllegalArgumentException("The actor catalog did not publish the operation input schema")
        val violations = decoder.validateSchemaDocument(schema, input)
        require(violations.isEmpty()) { "Operation input is invalid: ${violations.joinToString { "${it.path}: ${it.message}" }}" }
        val id = UUID.randomUUID().toString()
        val json = input.toJsonValue()
        val entity = OfflineIntentEntity(scope.key, id, idempotencyKey, scope.businessScope.toWire().toString(), catalog.revision,
            operation.operationRef, JSONObject().put("id", operation.inputSchema.id).put("version", operation.inputSchema.version).toString(),
            encode(json), expectedRevision, createdAt = System.currentTimeMillis())
        dao.insertIntent(entity)
        return id
    }

    suspend fun sync(scope: Scope, catalog: EngineApplicationCatalog): SyncResult = syncMutex.withLock {
        lastCatalogs[scope.key] = catalog
        var acknowledged = 0
        var firstError: Exception? = null
        if (!canSync(scope)) return@withLock SyncResult(0, KoraError("actor_changed", "Sign in again to sync this workspace", 401))
        try {
            val capabilities = decoder.decode("OfflineCapabilities", remote.capabilities(scope).toString())
            if (capabilities.getInt("protocol_version") != 2 || capabilities.getLong("catalog_revision") != catalog.revision || capabilities.getInt("max_batch_size") < 1) {
                return@withLock SyncResult(0, KoraError("offline_capabilities_changed", "Refresh the published catalog before syncing saved actions", 409))
            }
        } catch (error: Exception) {
            return@withLock SyncResult(0, error)
        }
        for (original in dao.pendingIntents(scope.key)) {
            if (!canSync(scope)) return@withLock SyncResult(acknowledged, KoraError("actor_changed", "Sign in again to sync this workspace", 401))
            var requestDispatched = false
            try {
                val operation = catalog.operations.singleOrNull { it.operationRef == original.operationRef }
                    ?: throw KoraError("catalog_revoked", "This action is no longer published. Your saved intent needs review.", 409)
                if (catalog.revision != original.catalogRevision || operation.availability != "available" || !operation.offlineEligible ||
                    original.inputSchema != JSONObject().put("id", operation.inputSchema.id).put("version", operation.inputSchema.version).toString()) {
                    throw KoraError("catalog_changed", "The actor catalog changed. Review this saved intent before syncing.", 409)
                }
                val input = decodeObject(original.encryptedInput)
                val schema = catalog.schemas.singleOrNull { it.ref == operation.inputSchema }?.schema
                    ?: throw KoraError("schema_revoked", "The input schema is no longer published", 409)
                val violations = decoder.validateSchemaDocument(schema, input)
                if (violations.isNotEmpty()) throw KoraError("schema_invalid", "Saved intent no longer matches its published schema", 409)
                val wire = JSONObject().put("local_operation_id", original.localOperationId)
                    .put("business_scope", scope.businessScope.toWire()).put("operation_ref", original.operationRef)
                    .put("input_schema", JSONObject(original.inputSchema)).put("input", input)
                    .put("idempotency_key", original.idempotencyKey).put("catalog_revision", original.catalogRevision)
                original.expectedRevision?.let { wire.put("expected_revision", it) }
                // Persist sent before dispatch so process death only recovers this same intent/key.
                val sent = original.copy(status = "sent", lastError = null)
                dao.putIntent(sent)
                val recoveredReceipt = if (original.status in setOf("sent", "accepted", "outcome_unknown")) {
                    remote.lookupIntent(scope, original.operationRef, original.idempotencyKey)
                        ?.also { validateReceipt(scope, catalog, operation, it) }
                } else null
                val resolved = recoveredReceipt ?: run {
                    requestDispatched = true
                    val result = remote.sendIntent(scope, wire)
                    receiptFor(result, original.localOperationId).also { validateReceipt(scope, catalog, operation, it) }
                }
                val receipt = resolved
                when (receipt.requiredString("status")) {
                    "completed" -> {
                        dao.putIntent(sent.copy(status = "applied", encryptedReceipt = encode(receipt), lastError = null))
                        acknowledged++
                    }
                    "rejected", "failed" -> dao.putIntent(sent.copy(status = "rejected", encryptedReceipt = encode(receipt), lastError = receipt.optJSONObject("error")?.optString("message")))
                    "conflict" -> dao.putIntent(sent.copy(status = "conflict", encryptedReceipt = encode(receipt), lastError = receipt.optJSONObject("error")?.optString("message")))
                    "accepted", "running" -> dao.putIntent(sent.copy(status = "accepted", encryptedReceipt = encode(receipt), lastError = null))
                    "outcome_unknown" -> dao.putIntent(sent.copy(status = "outcome_unknown", encryptedReceipt = encode(receipt), lastError = receipt.optJSONObject("error")?.optString("message")))
                    else -> dao.putIntent(sent.copy(status = "blocked", encryptedReceipt = encode(receipt), lastError = "Engine returned an unsupported receipt state"))
                }
            } catch (error: Exception) {
                val kora = error as? KoraError
                val status = when {
                    kora?.code == "module.operation_receipt_not_found" -> "sent"
                    kora?.code == "module.stale_catalog" || kora?.code == "module.stale_intent" || kora?.code == "module.operation_online_only" -> "blocked"
                    kora?.status == 409 -> "blocked"
                    kora?.status == 401 -> "sent"
                    kora?.status in 400..499 -> "blocked"
                    original.status == "queued" && !requestDispatched -> "queued"
                    else -> "outcome_unknown"
                }
                dao.putIntent(original.copy(status = status, lastError = error.message))
                firstError = firstError ?: error
            }
        }
        SyncResult(acknowledged, firstError)
    }

    private suspend fun validateReceipt(scope: Scope, catalog: EngineApplicationCatalog, operation: EnginePublishedOperation, receipt: JSONObject) {
        val parsed = decoder.decodeOperationReceipt(receipt.toString())
        require(parsed.operationRef == operation.operationRef && parsed.businessScope == scope.businessScope) { "Engine receipt does not match the intent scope" }
        if (parsed.status == "completed") {
            require(parsed.outputSchema == operation.outputSchema) { "Engine receipt output schema changed" }
            val output = parsed.output ?: throw IllegalArgumentException("Completed operation receipt has no output")
            val schema = catalog.schemas.singleOrNull { it.ref == operation.outputSchema }?.schema
                ?: throw IllegalArgumentException("Output schema is no longer published")
            require(decoder.validateSchemaDocument(schema, output).isEmpty()) { "Engine output did not match its published schema" }
        }
    }

    // Receipts returned by /offline/intents are individually contract-validated here.
    private fun receiptFor(response: JSONObject, localId: String): JSONObject {
        val decoded = decoder.decode("OfflineIntentsResponse", response.toString())
        val receipts = decoded.optJSONArray("receipts") ?: JSONArray()
        val matches = (0 until receipts.length()).map { receipts.getJSONObject(it) }.filter { it.optString("local_operation_id") == localId }
        require(receipts.length() == 1 && matches.size == 1) { "Engine did not return exactly one receipt for this offline intent" }
        return matches.single().requiredObject("receipt")
    }

    suspend fun replaceSnapshot(scope: Scope, owner: String, rows: List<Pair<String, Any>>, complete: Boolean) = db.withTransaction {
        if (complete) dao.clearFacts(scope.key, owner)
        rows.forEach { (key, value) -> dao.putFact(OfflineFactEntity(scope.key, owner, key, encode(value), false, System.currentTimeMillis())) }
    }

    fun observeIntents(scope: Scope) = dao.observeIntents(scope.key)
    suspend fun hasPending(scope: Scope) = dao.pendingIntents(scope.key).isNotEmpty()

    suspend fun observeCatalogThenSync(scope: Scope): SyncResult {
        val capabilities = remote.capabilities(scope)
        decoder.decode("OfflineCapabilities", capabilities.toString())
        val catalog = lastCatalogs[scope.key] ?: throw KoraError("catalog_required", "Refresh the actor catalog before syncing saved actions", 409)
        return sync(scope, catalog)
    }

    suspend fun bootstrap(scope: Scope, catalog: EngineApplicationCatalog, query: EnginePublishedQuery, input: Any, limit: Int? = null): JSONObject {
        require(catalog.businessScope == scope.businessScope && catalog.queries.any { it == query && it.offlineEligible } && !query.offlineFactKey.isNullOrBlank()) { "Query is not eligible for offline bootstrap" }
        validateCatalogValue(catalog, query.inputSchema, input)
        val source = JSONObject().put("owner_module_id", query.ownerModuleId).put("query_name", query.name)
            .put("input_schema", JSONObject().put("id", query.inputSchema.id).put("version", query.inputSchema.version)).put("input", input)
        val capabilities = decoder.decode("OfflineCapabilities", remote.capabilities(scope).toString())
        require(capabilities.getLong("catalog_revision") == catalog.revision && capabilities.getInt("protocol_version") == 2) { "Offline feed capabilities changed; refresh the catalog" }
        val pageLimit = minOf(limit ?: query.pageLimit, query.pageLimit, capabilities.getInt("max_page_size"))
        require(pageLimit > 0) { "Engine offline page size is unavailable" }
        val request = JSONObject().put("source", source).put("business_scope", scope.businessScope.toWire())
            .put("catalog_revision", catalog.revision).put("limit", pageLimit)
        var response = remote.bootstrap(scope, request)
        var decoded = decoder.decode("OfflineSnapshotPage", response.toString())
        validateSnapshotIdentity(scope, catalog, query, source, decoded, expectedHash = null, expectedSubject = null, expectedSnapshot = null)
        val rows = decoded.opt("rows") as? JSONArray ?: throw IllegalArgumentException("Snapshot rows must be an array")
        val all = mutableListOf<Pair<String, Any>>()
        appendSnapshotRows(rows, all, requireNotNull(query.offlineFactKey))
        val inputHash = decoded.getString("input_hash")
        val subjectId = decoded.getString("subject_id")
        val snapshotToken = decoded.getString("snapshot_token")
        val replayPosition = decoded.getString("replay_position")
        var pages = 1
        while (!decoded.getBoolean("complete")) {
            pages++
            require(pages <= 10_000) { "Offline snapshot page limit exceeded" }
            val next = decoded.optString("next_page_cursor").takeIf(String::isNotBlank)
                ?: throw IllegalArgumentException("Incomplete snapshot omitted its next page cursor")
            request.put("snapshot_token", snapshotToken).put("page_cursor", next).put("input_hash", inputHash)
            response = remote.bootstrap(scope, request)
            decoded = decoder.decode("OfflineSnapshotPage", response.toString())
            validateSnapshotIdentity(scope, catalog, query, source, decoded, inputHash, subjectId, snapshotToken, replayPosition)
            val page = decoded.optJSONArray("rows") ?: throw IllegalArgumentException("Snapshot rows must be an array")
            appendSnapshotRows(page, all, requireNotNull(query.offlineFactKey))
        }
        validateCatalogValue(catalog, query.outputSchema, JSONArray(all.map { it.second }))
        require(all.map { it.first }.distinct().size == all.size) { "Offline snapshot contains duplicate fact keys" }
        val checkpoint = JSONObject().put("snapshot_token", snapshotToken)
            .put("input_hash", inputHash).put("replay_position", replayPosition)
            .put("catalog_revision", catalog.revision).put("schema_revision", JSONObject().put("id", query.outputSchema.id).put("version", query.outputSchema.version))
            .put("source", source).put("business_scope", scope.businessScope.toWire()).put("subject_id", subjectId)
        db.withTransaction {
            replaceSnapshot(scope, query.ownerModuleId, all, complete = true)
            dao.putCheckpoint(OfflineCheckpointEntity(scope.key, query.ownerModuleId, query.name, checkpoint.getString("input_hash"), encode(checkpoint), catalog.revision))
        }
        return checkpoint
    }

    suspend fun pull(scope: Scope, catalog: EngineApplicationCatalog, query: EnginePublishedQuery, input: Any, checkpoint: JSONObject, limit: Int = 100): JSONObject {
        require(catalog.businessScope == scope.businessScope && catalog.queries.any { it == query && it.offlineEligible })
        validateCatalogValue(catalog, query.inputSchema, input)
        val source = JSONObject().put("owner_module_id", query.ownerModuleId).put("query_name", query.name)
            .put("input_schema", JSONObject().put("id", query.inputSchema.id).put("version", query.inputSchema.version)).put("input", input)
        val capabilities = decoder.decode("OfflineCapabilities", remote.capabilities(scope).toString())
        require(capabilities.getLong("catalog_revision") == catalog.revision && capabilities.getInt("protocol_version") == 2) { "Offline feed capabilities changed; refresh the catalog" }
        val pageLimit = minOf(limit, query.pageLimit, capabilities.getInt("max_page_size"))
        require(pageLimit > 0) { "Engine offline page size is unavailable" }
        val inputHash = checkpoint.getString("input_hash")
        require(checkpoint.getLong("catalog_revision") == catalog.revision && checkpoint.getJSONObject("business_scope").toBusinessScope() == scope.businessScope &&
            checkpoint.getString("subject_id") == scope.userId && sameSource(checkpoint.getJSONObject("source"), source) &&
            inputHash == sha256(input.toString()) && schemaRef(checkpoint.getJSONObject("schema_revision")) == query.outputSchema) {
            "Saved offline checkpoint no longer matches the active actor catalog and source"
        }
        val request = JSONObject().put("source", source).put("business_scope", scope.businessScope.toWire()).put("catalog_revision", catalog.revision)
            .put("snapshot_token", checkpoint.getString("snapshot_token")).put("input_hash", inputHash)
            .put("replay_position", checkpoint.getString("replay_position")).put("limit", pageLimit)
        checkpoint.optString("through_cursor").takeIf(String::isNotBlank)?.let { request.put("through_cursor", it) }
        checkpoint.optString("page_cursor").takeIf(String::isNotBlank)?.let { request.put("page_cursor", it) }
        var response = decoder.decode("OfflinePullResponse", remote.pull(scope, request).toString())
        var pages = 0
        var expectedThroughCursor = checkpoint.optString("through_cursor").takeIf(String::isNotBlank)
        while (true) {
            pages++
            require(pages <= 10_000) { "Offline feed page limit exceeded" }
            validatePullIdentity(scope, catalog, query, source, checkpoint, response)
            val changes = response.optJSONArray("changes") ?: JSONArray()
            for (i in 0 until changes.length()) {
                val change = changes.getJSONObject(i)
                if (change.getBoolean("deleted")) continue
                val data = change.optJSONObject("data") ?: throw IllegalArgumentException("Non-deletion feed change omitted its full row")
                validateQueryRow(catalog, query.outputSchema, data)
                require(factKey(data, requireNotNull(query.offlineFactKey)) == change.getString("fact_key")) { "Offline feed fact key does not match the published row identity" }
            }
            val nextCursor = response.optString("next_page_cursor").takeIf(String::isNotBlank)
            if (response.getBoolean("has_more")) require(nextCursor != null) { "Feed page declared more changes without a page cursor" }
            val throughCursor = response.getString("through_cursor")
            require(throughCursor.isNotBlank()) { "Feed page omitted its through cursor" }
            require(expectedThroughCursor == null || expectedThroughCursor == throughCursor) { "Engine feed through cursor changed during pagination" }
            expectedThroughCursor = throughCursor
            if (!response.getBoolean("has_more")) require(nextCursor == null) { "Feed page returned a cursor after the final page" }
            val nextCheckpoint = JSONObject(checkpoint.toString())
                .put("through_cursor", throughCursor)
            if (nextCursor != null) nextCheckpoint.put("page_cursor", nextCursor) else {
                nextCheckpoint.remove("page_cursor")
                nextCheckpoint.remove("through_cursor")
                nextCheckpoint.put("replay_position", throughCursor)
            }
            db.withTransaction {
                applyChanges(scope, query.ownerModuleId, changes)
                dao.putCheckpoint(OfflineCheckpointEntity(scope.key, query.ownerModuleId, query.name, inputHash, encode(nextCheckpoint), catalog.revision))
            }
            if (nextCursor == null) break
            request.put("through_cursor", throughCursor).put("page_cursor", nextCursor)
            response = decoder.decode("OfflinePullResponse", remote.pull(scope, request).toString())
        }
        return response
    }

    private fun validateSnapshotIdentity(scope: Scope, catalog: EngineApplicationCatalog, query: EnginePublishedQuery, source: JSONObject, page: JSONObject, expectedHash: String?, expectedSubject: String?, expectedSnapshot: String?, expectedReplayPosition: String? = null) {
        require(page.getJSONObject("business_scope").toBusinessScope() == scope.businessScope && page.getLong("catalog_revision") == catalog.revision &&
            page.getString("subject_id") == scope.userId && schemaRef(page.getJSONObject("schema_revision")) == query.outputSchema &&
            page.getString("fact_key_field") == query.offlineFactKey &&
            page.getJSONObject("source").let { sameSource(it, source) } && page.getString("input_hash") == sha256(source.get("input").toString()) &&
            (expectedHash == null || page.getString("input_hash") == expectedHash) &&
            (expectedSubject == null || page.getString("subject_id") == expectedSubject) &&
            (expectedSnapshot == null || page.getString("snapshot_token") == expectedSnapshot) &&
            (expectedReplayPosition == null || page.getString("replay_position") == expectedReplayPosition)) {
            "Engine snapshot page identity changed during bootstrap"
        }
    }

    private fun validatePullIdentity(scope: Scope, catalog: EngineApplicationCatalog, query: EnginePublishedQuery, source: JSONObject, checkpoint: JSONObject, page: JSONObject) {
        require(page.getJSONObject("business_scope").toBusinessScope() == scope.businessScope && page.getLong("catalog_revision") == catalog.revision &&
            page.getString("subject_id") == checkpoint.getString("subject_id") && schemaRef(page.getJSONObject("schema_revision")) == query.outputSchema &&
            page.getJSONObject("source").let { sameSource(it, source) } && page.getString("input_hash") == checkpoint.getString("input_hash") &&
            page.getString("snapshot_token") == checkpoint.getString("snapshot_token") && page.getString("replay_position") == checkpoint.getString("replay_position")) {
            "Engine feed page identity changed during replay"
        }
        checkpoint.optString("through_cursor").takeIf(String::isNotBlank)?.let { require(page.getString("through_cursor") == it) { "Engine feed through cursor changed during pagination" } }
    }

    private fun sameSource(a: JSONObject, b: JSONObject): Boolean =
        a.optString("owner_module_id") == b.optString("owner_module_id") && a.optString("query_name") == b.optString("query_name") &&
            a.optJSONObject("input_schema")?.toString() == b.optJSONObject("input_schema")?.toString() && a.opt("input")?.toString() == b.opt("input")?.toString()

    private fun schemaRef(value: JSONObject) = EngineSchemaRef(value.getString("id"), value.getInt("version"))

    private fun appendSnapshotRows(rows: JSONArray, target: MutableList<Pair<String, Any>>, factKeyField: String) {
        for (i in 0 until rows.length()) {
            val row = rows.optJSONObject(i) ?: throw IllegalArgumentException("Snapshot row must be an object")
            target += factKey(row, factKeyField) to row
        }
    }

    private fun factKey(row: JSONObject, field: String): String = row.opt(field)?.takeUnless { it == JSONObject.NULL }?.toString()
        ?.takeIf(String::isNotBlank)?.also { require(it.length <= 256 && (row.opt(field) is String || row.opt(field) is Number)) }
        ?: throw IllegalArgumentException("Offline source row is missing its declared stable fact key")

    suspend fun savedCheckpoint(scope: Scope, catalog: EngineApplicationCatalog, query: EnginePublishedQuery, input: Any): JSONObject? {
        val hash = sha256(input.toString())
        val saved = dao.checkpoint(scope.key, query.ownerModuleId, query.name, hash) ?: return null
        if (saved.catalogRevision != catalog.revision) return null
        return decodeObject(saved.encryptedCheckpoint)
    }

    private fun sha256(value: String) = java.security.MessageDigest.getInstance("SHA-256").digest(value.toByteArray(Charsets.UTF_8)).joinToString("") { "%02x".format(it.toInt() and 0xff) }

    private fun validateCatalogValue(catalog: EngineApplicationCatalog, ref: EngineSchemaRef, value: Any) {
        val schema = catalog.schemas.singleOrNull { it.ref == ref }?.schema ?: throw IllegalArgumentException("Catalog schema ${ref.id} v${ref.version} is missing")
        val violations = decoder.validateSchemaDocument(schema, value)
        require(violations.isEmpty()) { "Value failed ${ref.id} v${ref.version}: ${violations.joinToString { "${it.path}: ${it.message}" }}" }
    }

    private fun validateQueryRow(catalog: EngineApplicationCatalog, ref: EngineSchemaRef, row: JSONObject) {
        val schema = catalog.schemas.singleOrNull { it.ref == ref }?.schema ?: throw IllegalArgumentException("Catalog schema ${ref.id} v${ref.version} is missing")
        val rowSchema = if (schema.optString("type") == "array") schema.optJSONObject("items")
            ?: throw IllegalArgumentException("Published query array schema has no item schema") else schema
        val violations = decoder.validateSchemaDocument(rowSchema, row)
        require(violations.isEmpty()) { "Feed row failed ${ref.id} v${ref.version}: ${violations.joinToString { "${it.path}: ${it.message}" }}" }
    }

    suspend fun applyChanges(scope: Scope, owner: String, changes: JSONArray) = db.withTransaction {
        for (i in 0 until changes.length()) {
            val change = changes.getJSONObject(i)
            val factKey = change.requiredString("fact_key")
            val deleted = change.getBoolean("deleted")
            val data = if (deleted) null else change.optJSONObject("data") ?: throw IllegalArgumentException("Feed change data must be an object")
            dao.putFact(OfflineFactEntity(scope.key, owner, factKey, data?.let(::encode), deleted, System.currentTimeMillis()))
        }
    }

    private fun Any.toJsonValue(): Any = when (this) {
        is JSONObject, is JSONArray -> this
        is Map<*, *> -> JSONObject(this)
        is List<*> -> JSONArray(this)
        else -> this
    }
}
