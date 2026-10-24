package com.kora.mobile.data.offline

import androidx.room.withTransaction
import com.kora.mobile.core.database.*
import com.kora.mobile.core.security.PayloadCipher
import com.kora.mobile.domain.model.KoraError
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** UI reads only these rows. Network responses update their baseline, never the UI directly. */
class OfflineRecordRepository(
    private val db: KoraDatabase,
    private val cipher: PayloadCipher,
    private val remote: RecordRemoteDataSource,
    private val now: () -> Long = System::currentTimeMillis,
    private val canSync: (Scope) -> Boolean = { true },
) : RecordRepository, SyncCoordinator {
    private val dao = db.localDao
    private val syncMutex = Mutex()
    private val refreshMutex = Mutex()
    private val pullMutex = Mutex()
    private val bootstrapMutex = Mutex()

    private fun decode(value: String): JSONObject = cipher.decrypt(value)?.let(::JSONObject)
        ?: throw IllegalStateException("Saved data could not be decrypted. It has been kept on this device.")
    private fun encode(value: JSONObject) = cipher.encrypt(value.toString())
    private fun revision(value: JSONObject): String? = value.optString("_version").ifBlank { value.optString("modified") }.takeIf { it.isNotBlank() && it != "null" }
    private fun target(operation: LocalMutation, record: LocalRecord) = operation.localId == record.localId || (operation.localId == null && operation.doctype == record.doctype && (record.localId == "local:${operation.id}" || operation.recordName != null && operation.recordName == record.serverId))
    private fun merged(base: JSONObject, patch: JSONObject): JSONObject = JSONObject(base.toString()).apply { patch.keys().forEach { put(it, patch.opt(it)) } }

    private suspend fun render(record: LocalRecord): JSONObject {
        val pending = dao.queuedMutations(record.scopeKey).filter { target(it, record) }
        return decode(record.encryptedPayload).put("name", record.serverId ?: record.localId)
            .put("_local_id", record.localId)
            .put("_sync_status", pending.firstOrNull { it.status in setOf("conflict", "rejected", "blocked") }?.status ?: if (pending.isEmpty()) "clean" else "pending")
    }

    override fun observeRecords(scope: Scope, doctype: String): Flow<List<JSONObject>> = combine(dao.observeRecords(scope.key, doctype), dao.observeMutations(scope.key)) { rows, _ -> rows.map { render(it) } }

    override suspend fun records(scope: Scope, doctype: String): JSONArray {
        importLegacy(scope, doctype)
        return JSONArray(dao.records(scope.key, doctype).map { render(it) })
    }

    override suspend fun record(scope: Scope, doctype: String, name: String): JSONObject? {
        importLegacy(scope, doctype)
        return dao.record(scope.key, doctype, name)?.let { render(it) }
    }

    /** Import cached records and old queued writes without changing their retry identities. */
    private suspend fun importLegacy(scope: Scope, doctype: String) = db.withTransaction {
        val marker = "records-imported:$doctype"
        if (dao.getEntry(scope.key, marker) != null) return@withTransaction
        dao.getEntry(scope.key, "records:$doctype")?.let { entry ->
            val raw = cipher.decrypt(entry.encryptedPayload) ?: throw IllegalStateException("Saved records could not be decrypted")
            val parsed = org.json.JSONTokener(raw).nextValue()
            val rows = if (parsed is JSONArray) parsed else (parsed as? JSONObject)?.optJSONArray("items") ?: JSONArray()
            for (i in 0 until rows.length()) rows.optJSONObject(i)?.let { upsertServer(scope, doctype, it) }
        }
        for (mutation in dao.queuedMutations(scope.key).filter { it.doctype == doctype && it.localId == null }) {
            val old = mutation.recordName?.let { dao.record(scope.key, doctype, it) }
            val id = old?.localId ?: "local:${mutation.id}"
            val data = merged(old?.let { decode(it.encryptedPayload) } ?: JSONObject(), decode(mutation.encryptedPayload))
            dao.putRecord(LocalRecord(scope.key, doctype, id, mutation.recordName, encode(data), old?.encryptedServerPayload, old?.serverRevision, mutation.createdAt))
            // Do not change localId on legacy operations: the transport must retain its original request shape.
        }
        dao.putEntry(LocalEntry(scope.key, marker, cipher.encrypt("true"), now(), "migration"))
    }

    override suspend fun save(scope: Scope, doctype: String, name: String?, payload: JSONObject, idempotencyKey: String, displayName: String?, draftId: String?): String {
        importLegacy(scope, doctype)
        return db.withTransaction {
            val existing = name?.let { dao.record(scope.key, doctype, it) }
            val localId = existing?.localId ?: "local:${UUID.randomUUID()}"
            val serverId = existing?.serverId ?: name?.takeUnless { it.startsWith("local:") }
            val clean = stripLocalMetadata(payload)
            val record = LocalRecord(scope.key, doctype, localId, serverId,
                encode(merged(existing?.let { decode(it.encryptedPayload) } ?: JSONObject(), clean)),
                existing?.encryptedServerPayload, existing?.serverRevision, now())
            val queue = dao.queuedMutations(scope.key)
            val timestamp = maxOf(now(), (queue.maxOfOrNull { it.createdAt } ?: 0) + 1)
            val operation = LocalMutation(UUID.randomUUID().toString(), scope.key, doctype, serverId,
                idempotencyKey, encode(clean), timestamp, displayName,
                localId = localId, baseRevision = existing?.serverRevision)
            dao.commitLocal(record, operation, draftId)
            localId
        }
    }

    suspend fun saveAction(scope: Scope, actionId: String, request: JSONObject, idempotencyKey: String, recordType: String, projection: JSONObject, effect: JSONObject? = null, draftId: String? = null) = db.withTransaction {
        val id = UUID.randomUUID().toString()
        val localId = "local:$id"
        val timestamp = maxOf(now(), (dao.queuedMutations(scope.key).maxOfOrNull { it.createdAt } ?: 0) + 1)
        val record = LocalRecord(scope.key, recordType, localId, null, encode(projection), null, null, timestamp)
        val mutation = LocalMutation(id, scope.key, "@view-action/$actionId", null, idempotencyKey, encode(request), timestamp, projection.optString("reference"), localId = localId)
        dao.commitLocal(record, mutation, draftId)
        if (effect != null) {
            val type = effect.optString("doctype")
            val field = effect.optString("quantity_field")
            val lines = effect.optJSONArray("lines") ?: JSONArray()
            if (type.isBlank() || field.isBlank() || lines.length() == 0 || (0 until lines.length()).any { lines.optJSONObject(it)?.optString("quantity", "0")?.toBigDecimalOrNull() in setOf(null, java.math.BigDecimal.ZERO) }) throw KoraError("invalid_local_effect", "This change is missing quantity details")
            dao.putEntry(LocalEntry(scope.key, "effect:$id", encode(effect), now(), "local-effect"))
            val names = linkedSetOf<String>()
            for (i in 0 until lines.length()) lines.optJSONObject(i)?.optString("name")?.takeIf(String::isNotBlank)?.let { names += it }
            for (name in names) recomputeQuantity(scope, type, name, field, requireAvailable = true)
        }
    }

    private suspend fun relatedEffects(scope: Scope, type: String, record: LocalRecord, field: String): List<Pair<LocalMutation, JSONObject>> {
        return dao.entries(scope.key, "local-effect").mapNotNull { entry ->
            val effect = decode(entry.encryptedPayload)
            if (effect.optString("doctype") != type || effect.optString("quantity_field") != field) return@mapNotNull null
            val lines = effect.optJSONArray("lines") ?: return@mapNotNull null
            if ((0 until lines.length()).none { line -> lines.optJSONObject(line)?.optString("name") in setOf(record.localId, record.serverId) }) return@mapNotNull null
            val operation = dao.mutation(scope.key, entry.entryKey.removePrefix("effect:")) ?: return@mapNotNull null
            operation to effect
        }
    }

    private suspend fun recomputeQuantity(scope: Scope, type: String, name: String, field: String, requireAvailable: Boolean = false) {
        val record = dao.record(scope.key, type, name) ?: throw KoraError("stock_unavailable", "Download this item before recording a sale")
        val baseline = record.encryptedServerPayload?.let { decode(it) }
            ?: dao.queuedMutations(scope.key).firstOrNull { it.doctype == type && it.localId == record.localId && it.recordName == null }
                ?.let { decode(it.encryptedPayload) }
            ?: throw KoraError("stock_unavailable", "This item has no saved starting quantity")
        val productEdits = dao.queuedMutations(scope.key).filter { it.doctype == type && target(it, record) && it.status !in setOf("rejected", "conflict", "blocked") }
        val effective = productEdits.fold(baseline) { acc, op -> merged(acc, decode(op.encryptedPayload)) }
        val effects = relatedEffects(scope, type, record, field)
        var delta = java.math.BigDecimal.ZERO
        for ((operation, effect) in effects) {
            if (operation.status in setOf("rejected", "conflict", "blocked", "discarded")) continue
            val reconciled = effect.optJSONArray("reconciled") ?: JSONArray()
            if (operation.status == "acked" && (0 until reconciled.length()).any { reconciled.optString(it) in setOf(record.localId, record.serverId) }) continue
            val lines = effect.optJSONArray("lines") ?: continue
            for (i in 0 until lines.length()) lines.optJSONObject(i)?.takeIf { it.optString("name") in setOf(record.localId, record.serverId) }?.let { line ->
                delta += line.optString("quantity", "0").toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
            }
        }
        val available = effective.optString(field, "0").toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
        if (requireAvailable && available.subtract(delta) < java.math.BigDecimal.ZERO) throw KoraError("stock_unavailable", "Not enough saved quantity is available for this change")
        effective.put(field, available.subtract(delta).max(java.math.BigDecimal.ZERO).toPlainString())
        dao.putRecord(record.copy(encryptedPayload = encode(effective), updatedAt = now()))
    }

    private suspend fun effectProducts(scope: Scope, operation: LocalMutation): List<Triple<String, String, String>> {
        val effect = dao.getEntry(scope.key, "effect:${operation.id}")?.let { decode(it.encryptedPayload) } ?: return emptyList()
        val type = effect.optString("doctype")
        val field = effect.optString("quantity_field")
        val lines = effect.optJSONArray("lines") ?: return emptyList()
        return (0 until lines.length()).mapNotNull { lines.optJSONObject(it)?.optString("name")?.takeIf(String::isNotBlank) }
            .distinct().map { Triple(type, it, field) }
    }

    /** Called inside a transaction; preserves the local overlay when a refresh arrives. */
    private suspend fun upsertServer(scope: Scope, doctype: String, data: JSONObject) {
        val name = data.optString("name").takeIf { it.isNotBlank() } ?: return
        val old = dao.record(scope.key, doctype, name)
        val allEffects = dao.entries(scope.key, "local-effect").mapNotNull { entry ->
            val effect = decode(entry.encryptedPayload)
            if (effect.optString("doctype") != doctype || (0 until (effect.optJSONArray("lines")?.length() ?: 0)).none { effect.optJSONArray("lines")?.optJSONObject(it)?.optString("name") in setOf(name, old?.localId) }) null
            else dao.mutation(scope.key, entry.entryKey.removePrefix("effect:"))?.let { it to effect }
        }
        if (old != null && allEffects.any { it.first.status in setOf("queued", "sent") }) return
        for ((operation, effect) in allEffects.filter { it.first.status == "acked" }) {
            val reconciled = effect.optJSONArray("reconciled") ?: JSONArray().also { effect.put("reconciled", it) }
            if ((0 until reconciled.length()).none { reconciled.optString(it) == name }) reconciled.put(name)
            dao.putEntry(LocalEntry(scope.key, "effect:${operation.id}", encode(effect), now(), "local-effect"))
        }
        val localId = old?.localId ?: "server:$doctype:$name"
        val pending = dao.queuedMutations(scope.key).filter { it.doctype == doctype && (it.localId == localId || it.recordName == name) }
        val effective = pending.fold(data) { acc, op -> merged(acc, decode(op.encryptedPayload)) }
        dao.putRecord(LocalRecord(scope.key, doctype, localId, name, encode(effective), encode(data), revision(data), old?.updatedAt ?: now()))
    }

    suspend fun hasPullCursor(scope: Scope) = dao.getEntry(scope.key, "offline-pull-cursor") != null

    suspend fun isCovered(scope: Scope, doctype: String): Boolean = typeRoutes(scope).let { routes ->
        routes.keys().asSequence().any { routes.optString(it) == doctype }
    }

    private suspend fun typeRoutes(scope: Scope): JSONObject = dao.getEntry(scope.key, "offline-type-routes")
        ?.let { decode(it.encryptedPayload) } ?: JSONObject()

    /** A snapshot page and its checkpoint are persisted together; final installation is atomic. */
    suspend fun capabilities(scope: Scope): JSONObject = remote.capabilities(scope)

    suspend fun bootstrap(scope: Scope, routes: Map<String, String>, expectedGeneration: Long? = null, onPage: (Int) -> Unit = {}) = bootstrapMutex.withLock {
        val selection = JSONObject(routes)
        val saved = dao.getEntry(scope.key, "offline-bootstrap-progress")?.let { decode(it.encryptedPayload) }
        val progress = saved?.takeIf { savedPage ->
            savedPage.optJSONObject("routes")?.let { old ->
                old.length() == routes.size && routes.all { (name, route) -> old.optString(name) == route }
            } == true
        }
        if (saved != null && progress == null) db.withTransaction {
            saved.optString("token").takeIf(String::isNotBlank)?.let { dao.deleteEntries(scope.key, "snapshot:$it") }
            dao.deleteEntry(scope.key, "offline-bootstrap-progress")
        }
        var token = progress?.optString("token")?.takeIf(String::isNotBlank)
        var next = progress?.optString("next")?.takeIf(String::isNotBlank)
        val startedAt = progress?.optLong("started_at")?.takeIf { it > 0 } ?: now()
        var pages = progress?.optInt("pages") ?: 0
        try {
            while (true) {
                val page = remote.bootstrap(scope, routes.keys.toList(), token, next)
                val pageToken = page.optString("snapshot_token").takeIf(String::isNotBlank)
                    ?: throw KoraError("invalid_bootstrap", "Kora did not identify the workspace snapshot", retryable = true)
                if (token != null && token != pageToken) throw KoraError("invalid_bootstrap", "Kora changed the workspace snapshot", retryable = true)
                token = pageToken
                if (expectedGeneration != null && page.optLong("generation", -1) != expectedGeneration) {
                    throw KoraError("offline.generation_changed", "Workspace configuration changed during download. Download it again.", 409)
                }
                val category = "snapshot:$pageToken"
                val records = page.optJSONArray("records") ?: throw KoraError("invalid_bootstrap", "Kora omitted workspace records", retryable = true)
                val index = next?.toIntOrNull() ?: 0
                val complete = page.optBoolean("complete")
                val cursor = page.optString("pull_cursor")
                if (complete && cursor.isBlank()) throw KoraError("invalid_bootstrap", "Kora omitted the change cursor", retryable = true)
                val nextPage = page.optString("next_page_token")
                if (!complete && (nextPage.isBlank() || nextPage == next)) throw KoraError("invalid_bootstrap", "Kora did not advance the workspace page", retryable = true)
                db.withTransaction {
                    for (i in 0 until records.length()) {
                        val item = records.optJSONObject(i) ?: throw KoraError("invalid_bootstrap", "Kora returned an invalid record", retryable = true)
                        val name = item.optString("doctype")
                        if (!routes.containsKey(name) || item.optJSONObject("record") == null) throw KoraError("invalid_bootstrap", "Kora returned a record outside this workspace", retryable = true)
                        dao.putEntry(LocalEntry(scope.key, "$category:${index + i}", encode(item), now(), category))
                    }
                    if (complete) {
                        val staged = dao.entries(scope.key, category)
                        val present = mutableMapOf<String, MutableSet<String>>()
                        for (entry in staged) {
                            val item = decode(entry.encryptedPayload)
                            val route = routes[item.getString("doctype")] ?: continue
                            val record = item.getJSONObject("record")
                            val id = record.optString("name")
                            if (id.isBlank()) throw KoraError("invalid_bootstrap", "Kora returned a record without an ID", retryable = true)
                            present.getOrPut(route) { mutableSetOf() } += id
                            upsertServer(scope, route, record)
                        }
                        for (route in routes.values.distinct()) {
                            for (old in dao.records(scope.key, route)) {
                                if (old.serverId != null && old.serverId !in present.getOrPut(route) { mutableSetOf() } && old.updatedAt < startedAt) removeOrConflict(scope, old)
                            }
                            dao.putEntry(LocalEntry(scope.key, "records-downloaded:$route", cipher.encrypt("${present[route]?.size ?: 0}"), now(), "coverage"))
                        }
                        val previousRoutes = typeRoutes(scope)
                        for (name in previousRoutes.keys()) {
                            val oldRoute = previousRoutes.optString(name)
                            if (oldRoute.isBlank() || oldRoute in routes.values) continue
                            for (old in dao.records(scope.key, oldRoute)) removeOrConflict(scope, old)
                            dao.deleteEntry(scope.key, "records-downloaded:$oldRoute")
                        }
                        dao.putEntry(LocalEntry(scope.key, "offline-type-routes", encode(selection), now(), "coverage"))
                        dao.putEntry(LocalEntry(scope.key, "offline-pull-cursor", cipher.encrypt(cursor), now(), "coverage"))
                        dao.deleteEntry(scope.key, "offline-bootstrap-progress")
                        dao.deleteEntries(scope.key, category)
                    } else {
                        dao.putEntry(LocalEntry(scope.key, "offline-bootstrap-progress", encode(JSONObject().put("token", pageToken).put("next", nextPage).put("routes", selection).put("started_at", startedAt).put("pages", pages + 1)), now(), "coverage"))
                    }
                }
                pages++
                onPage(pages)
                if (complete) break
                next = nextPage
            }
        } catch (error: KoraError) {
            if (error.code == "offline.rebootstrap_required" || error.status == 409) {
                val oldToken = token
                db.withTransaction {
                    dao.deleteEntry(scope.key, "offline-bootstrap-progress")
                    if (oldToken != null) dao.deleteEntries(scope.key, "snapshot:$oldToken")
                }
            }
            throw error
        }
    }

    private suspend fun removeOrConflict(scope: Scope, record: LocalRecord) {
        val pending = dao.queuedMutations(scope.key).filter { target(it, record) }
        if (pending.isEmpty()) dao.deleteRecord(scope.key, record.doctype, record.localId)
        else for (op in pending) dao.putMutation(op.copy(status = "conflict", lastError = "This record was removed or is no longer accessible on the server. Review your saved changes."))
    }

    suspend fun pull(scope: Scope) = pullMutex.withLock {
        val routes = typeRoutes(scope)
        if (routes.length() == 0) return@withLock
        repeat(1000) {
            val stored = dao.getEntry(scope.key, "offline-pull-cursor") ?: return@withLock
            val cursor = cipher.decrypt(stored.encryptedPayload) ?: throw KoraError("invalid_cursor", "The saved change cursor could not be read")
            val page = remote.pull(scope, cursor)
            val next = page.optString("next_cursor").takeIf(String::isNotBlank)
                ?: throw KoraError("invalid_pull", "Kora omitted the next change cursor", retryable = true)
            val changes = page.optJSONArray("changes") ?: throw KoraError("invalid_pull", "Kora omitted record changes", retryable = true)
            val tombstones = page.optJSONArray("tombstones") ?: throw KoraError("invalid_pull", "Kora omitted deletion markers", retryable = true)
            db.withTransaction {
                for (i in 0 until changes.length()) {
                    val item = changes.optJSONObject(i) ?: throw KoraError("invalid_pull", "Kora returned an invalid change", retryable = true)
                    val route = routes.optString(item.optString("doctype"))
                    if (route.isBlank()) continue
                    val record = item.optJSONObject("record") ?: throw KoraError("invalid_pull", "Kora returned an invalid record", retryable = true)
                    upsertServer(scope, route, record)
                }
                for (i in 0 until tombstones.length()) {
                    val marker = tombstones.optJSONObject(i) ?: continue
                    val route = routes.optString(marker.optString("doctype"))
                    if (route.isBlank()) continue
                    dao.record(scope.key, route, marker.optString("name"))?.let { removeOrConflict(scope, it) }
                }
                dao.putEntry(LocalEntry(scope.key, "offline-pull-cursor", cipher.encrypt(next), now(), "coverage"))
            }
            if (!page.optBoolean("has_more")) return@withLock
            if (next == cursor) throw KoraError("invalid_pull", "Kora did not advance the change cursor", retryable = true)
        }
        throw KoraError("pull_incomplete", "Kora has more changes than this sync run can process", retryable = true)
    }

    suspend fun refreshRecord(scope: Scope, doctype: String, name: String) {
        val local = dao.record(scope.key, doctype, name)
        val serverId = local?.serverId ?: name.takeUnless { it.startsWith("local:") } ?: return
        val fresh = remote.get(scope, doctype, serverId)
        db.withTransaction { upsertServer(scope, doctype, fresh) }
    }

    override suspend fun refresh(scope: Scope, doctype: String) = refreshMutex.withLock {
        importLegacy(scope, doctype)
        // REST compatibility: fetch every page, then replace the baseline atomically.
        // This is not a snapshot cursor protocol; don't delete missing rows on this path.
        val all = linkedMapOf<String, JSONObject>()
        var offset = 0
        while (true) {
            val page = remote.list(scope, doctype, offset, 100)
            var added = 0
            for (i in 0 until page.length()) page.optJSONObject(i)?.let { row ->
                val name = row.optString("name")
                if (name.isNotBlank() && all.put(name, row) == null) added++
            }
            if (page.length() == 0) break
            if (added == 0 || offset >= 100_000) throw KoraError("pagination_incomplete", "Kora did not return another page. The previous download has been kept.")
            offset += page.length()
        }
        db.withTransaction {
            all.values.forEach { upsertServer(scope, doctype, it) }
            dao.putEntry(LocalEntry(scope.key, "records-downloaded:$doctype", cipher.encrypt("${all.size}"), now(), "coverage"))
        }
    }

    override suspend fun sync(scope: Scope, force: Boolean): SyncResult = syncMutex.withLock {
        var count = 0
        var firstError: Exception? = null
        var retryAt: Long? = null
        val touched = linkedSetOf<String>()
        val queue = dao.queuedMutations(scope.key)
        val blockedTargets = mutableSetOf<String>()
        for (original in queue) {
            if (!canSync(scope)) return@withLock SyncResult(count, KoraError("actor_changed", "Sign in again to sync this workspace", 401))
            val target = original.localId ?: "${original.doctype}:${original.recordName ?: original.id}"
            if (target in blockedTargets || original.status in setOf("conflict", "rejected", "blocked")) {
                blockedTargets += target
                continue
            }
            if (!force && original.nextAttemptAt > now()) {
                retryAt = minOf(retryAt ?: Long.MAX_VALUE, original.nextAttemptAt)
                blockedTargets += target
                continue
            }
            try {
                val operation = db.withTransaction {
                    val current = dao.mutation(scope.key, original.id) ?: return@withTransaction null
                    if (current.status == "acked") return@withTransaction null
                    val record = current.localId?.let { dao.recordByLocalId(scope.key, it) }
                    val unresolvedBody = decode(current.encryptedPayload)
                    val body = if (current.attempts == 0) resolveReferences(scope, unresolvedBody) as JSONObject else unresolvedBody
                    val previous = queue.takeWhile { it.id != current.id }.any { it.localId != null && it.localId == current.localId }
                    current.copy(recordName = if (current.attempts == 0 && current.localId != null) record?.serverId else current.recordName,
                        baseRevision = if (current.attempts == 0 && previous) record?.serverRevision else current.baseRevision,
                        encryptedPayload = encode(body), status = "sent", attempts = current.attempts + 1, lastError = null).also { dao.putMutation(it) }
                } ?: continue
                val body = decode(operation.encryptedPayload)
                val result = remote.send(scope, operation, operation.recordName, body)
                db.withTransaction {
                    val action = operation.doctype.startsWith("@view-action/")
                    val old = operation.localId?.let { dao.recordByLocalId(scope.key, it) }
                        ?: operation.recordName?.let { dao.record(scope.key, operation.doctype, it) }
                        ?: dao.record(scope.key, operation.doctype, "local:${operation.id}")
                    val type = old?.doctype ?: if (action) "sale" else operation.doctype
                    val serverId = result.optString("name").takeIf { it.isNotBlank() }
                        ?: throw KoraError("invalid_receipt", "Kora did not confirm a record ID", retryable = true)
                    val duplicate = dao.record(scope.key, type, serverId)
                    if (old != null && duplicate != null && old.localId != duplicate.localId) dao.deleteRecord(scope.key, type, duplicate.localId)
                    val remaining = dao.queuedMutations(scope.key).filter { it.id != operation.id && old != null && target(it, old) }
                    val baseline = merged(if (action) old?.let { resolveReferences(scope, decode(it.encryptedPayload)) as JSONObject } ?: JSONObject() else body, result)
                    if (action) {
                        baseline.remove("_payment_state")
                        baseline.remove("_payment_intent")
                    }
                    val effective = remaining.fold(baseline) { acc, op -> merged(acc, decode(op.encryptedPayload)) }
                    val saved = LocalRecord(scope.key, type, old?.localId ?: "server:$type:$serverId", serverId,
                        encode(effective), encode(baseline), revision(result), now())
                    dao.acknowledge(saved, operation)
                    if (!action) {
                        val affectedFields = linkedSetOf<String>()
                        for (pending in dao.queuedMutations(scope.key).filter { it.doctype.startsWith("@view-action/") }) {
                            for ((effectType, effectName, field) in effectProducts(scope, pending)) {
                                if (effectType == type && effectName in setOf(saved.localId, saved.serverId)) affectedFields += field
                            }
                        }
                        for (field in affectedFields) recomputeQuantity(scope, type, saved.localId, field)
                    }
                    touched += type
                }
                if (operation.doctype.startsWith("@view-action/")) {
                    for ((type, name, _) in effectProducts(scope, operation)) {
                        try { refreshRecord(scope, type, name) }
                        catch (cancelled: CancellationException) { throw cancelled }
                        catch (_: Exception) { /* The provisional quantity remains until pull reconciles it. */ }
                    }
                }
                count++
            } catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) {
                val current = dao.mutation(scope.key, original.id) ?: original
                val kora = error as? KoraError
                val temporary = kora?.retryable == true || error is java.io.IOException || error is android.database.sqlite.SQLiteException
                val status = when {
                    temporary -> "queued"
                    kora?.status == 401 -> "queued"
                    kora?.code == "dependency_pending" -> "queued"
                    kora?.status == 409 -> "conflict"
                    else -> "rejected"
                }
                val next = if (temporary) now() + (30_000L * (1L shl current.attempts.coerceIn(0, 6))).coerceAtMost(1_800_000L) else 0
                db.withTransaction {
                    dao.putMutation(current.copy(status = status, lastError = error.message, nextAttemptAt = next))
                    if (current.doctype.startsWith("@view-action/") && status in setOf("rejected", "conflict")) {
                        for ((type, name, field) in effectProducts(scope, current)) recomputeQuantity(scope, type, name, field)
                        dao.deleteEntry(scope.key, "effect:${current.id}")
                    }
                }
                if (status == "conflict" && current.recordName != null && !current.doctype.startsWith("@view-action/")) {
                    try { refreshRecord(scope, current.doctype, current.recordName) }
                    catch (cancelled: CancellationException) { throw cancelled }
                    catch (_: Exception) { /* keep both the operation and last known baseline */ }
                }
                firstError = firstError ?: error
                blockedTargets += target
                if (temporary || kora?.status == 401) {
                    retryAt = next.takeIf { it > 0 }
                    break // endpoint failure affects every remaining operation
                }
            }
        }
        SyncResult(count, firstError, retryAt)
    }

    private suspend fun resolveReferences(scope: Scope, value: Any?): Any? = when (value) {
        is JSONObject -> JSONObject().apply { for (key in value.keys()) put(key, resolveReferences(scope, value.opt(key))) }
        is JSONArray -> JSONArray().apply { for (i in 0 until value.length()) put(resolveReferences(scope, value.opt(i))) }
        is String -> if (value.startsWith("local:")) {
            dao.recordByLocalId(scope.key, value)?.serverId ?: throw KoraError("dependency_pending", "Waiting for a linked record to sync")
        } else value
        else -> value
    }

    suspend fun retry(scope: Scope, id: String) {
        val operation = dao.mutation(scope.key, id) ?: return
        // Rejected/conflicting payloads need correction; replaying them unchanged cannot resolve them.
        if (operation.status !in setOf("conflict", "rejected", "acked")) dao.putMutation(operation.copy(nextAttemptAt = 0))
    }

    suspend fun acceptServer(scope: Scope, id: String) = db.withTransaction {
        val op = dao.mutation(scope.key, id) ?: return@withTransaction
        if (op.status !in setOf("conflict", "rejected")) return@withTransaction
        val record = op.localId?.let { dao.recordByLocalId(scope.key, it) }
        dao.putMutation(op.copy(status = "discarded"))
        if (record != null) {
            val remaining = dao.queuedMutations(scope.key).filter { target(it, record) }
            if (record.encryptedServerPayload == null && remaining.isEmpty()) dao.deleteRecord(scope.key, record.doctype, record.localId)
            else {
                val baseline = record.encryptedServerPayload?.let(::decode) ?: JSONObject()
                val effective = remaining.fold(baseline) { acc, pending -> merged(acc, decode(pending.encryptedPayload)) }
                dao.putRecord(record.copy(encryptedPayload = encode(effective)))
            }
        }
    }

    suspend fun correct(scope: Scope, id: String, payload: JSONObject, draftId: String?) = db.withTransaction {
        val op = dao.mutation(scope.key, id) ?: throw IllegalStateException("Saved change no longer exists")
        require(op.status in setOf("conflict", "rejected")) { "This change is not waiting for a correction" }
        require(!op.doctype.startsWith("@view-action/")) { "Review the sale before recording a correction" }
        val record = op.localId?.let { dao.recordByLocalId(scope.key, it) }
        dao.putMutation(op.copy(status = "discarded"))
        if (record != null) {
            // The correction is a complete editable document. Supersede only later
            // operations that have never been sent; their desired values are in the form.
            dao.queuedMutations(scope.key).filter { target(it, record) && it.attempts == 0 && it.createdAt > op.createdAt }
                .forEach { dao.putMutation(it.copy(status = "discarded")) }
        }
        save(scope, op.doctype, record?.localId ?: op.recordName, payload, UUID.randomUUID().toString(), op.displayName, draftId)
    }

    companion object {
        fun stripLocalMetadata(payload: JSONObject): JSONObject = JSONObject().apply {
            payload.keys().asSequence().filterNot { it.startsWith("_local") || it == "_sync_status" || it == "_version" }.forEach { put(it, payload.opt(it)) }
        }
    }
}
