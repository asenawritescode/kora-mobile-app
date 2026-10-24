package com.kora.mobile.data

import com.kora.mobile.core.database.LocalDao
import com.kora.mobile.core.database.LocalEntry
import com.kora.mobile.core.database.LocalMutation
import com.kora.mobile.core.database.KoraDatabase
import com.kora.mobile.data.offline.OfflineRecordRepository
import com.kora.mobile.data.offline.RestRecordDataSource
import com.kora.mobile.data.offline.OfflineCoverage
import com.kora.mobile.core.network.KoraHttpClient
import com.kora.mobile.core.security.SecretVault
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class WorkspaceRepository(private val http: KoraHttpClient, private val db: KoraDatabase, private val vault: SecretVault, private val canSync: (Scope) -> Boolean = { true }) {
    private val dao = db.localDao
    val localRecords = OfflineRecordRepository(db, vault, RestRecordDataSource(http), canSync = canSync)
    private val background = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshJobs = ConcurrentHashMap<String, Job>()
    private val _offline = MutableStateFlow(false)
    val offline: StateFlow<Boolean> = _offline
    private val _coverage = MutableStateFlow(OfflineCoverage())
    val coverage: StateFlow<OfflineCoverage> = _coverage
    private val downloadMutex = Mutex()
    var networkAvailable = true
    fun markNetworkUnavailable() { networkAvailable = false; _offline.value = true }
    private suspend fun <T> network(block: suspend () -> T): T = try {
        block().also { _offline.value = false }
    } catch (cancelled: CancellationException) { throw cancelled }
    catch (error: Exception) {
        if ((error as? com.kora.mobile.domain.model.KoraError)?.retryable == true) _offline.value = true
        throw error
    }
    private fun refresh(key: String, block: suspend () -> Unit) {
        if (!networkAvailable) return
        synchronized(refreshJobs) {
            if (refreshJobs[key]?.isActive == true) return
            refreshJobs[key] = background.launch { try { network(block) } catch (cancelled: CancellationException) { throw cancelled } catch (_: Exception) { /* saved data stays visible */ } }
        }
    }
    fun copySession(fromOrigin: String, toOrigin: String, siteRef: String?) = http.copySession(fromOrigin, toOrigin, siteRef)
    // Compatibility for old callers: cache use is no longer confused with connectivity.
    val lastResponseWasCached: Boolean get() = _offline.value

    suspend fun probe(scope: Scope) { http.get(scope, "/api/ping") }

    suspend fun restoreCoverage(scope: Scope) {
        val completed = dao.getEntry(scope.key, "offline-prepared")
        _coverage.value = OfflineCoverage(completedAt = completed?.fetchedAt)
    }

    suspend fun prepareOffline(scope: Scope) = downloadMutex.withLock {
        _coverage.value = _coverage.value.copy(downloading = true, error = null)
        try {
            val generation = network { localRecords.capabilities(scope) }.optLong("generation", -1)
            if (generation < 0) throw com.kora.mobile.domain.model.KoraError("invalid_capabilities", "Kora did not identify the workspace configuration")
            suspend fun store(key: String, category: String, fetch: suspend () -> Any): Any {
                val result = network(fetch)
                dao.putEntry(LocalEntry(scope.key, key, vault.encrypt(result.toString()), System.currentTimeMillis(), category))
                return result
            }
            val nav = store("navigation", "navigation") { http.get(scope, "/api/v1/system/navigation") } as JSONObject
            store("tenant-branding", "navigation") { http.get(scope, "/api/v1/system/branding") }
            val types = linkedMapOf<String, String>()
            val modules = nav.optJSONArray("modules") ?: JSONArray()
            for (i in 0 until modules.length()) {
                val entries = modules.optJSONObject(i)?.optJSONArray("doctypes") ?: continue
                for (j in 0 until entries.length()) entries.optJSONObject(j)?.let { type ->
                    if (!type.optBoolean("is_child")) types[type.optString("name")] = type.optString("resource_name").ifBlank { type.optString("name") }
                }
            }
            _coverage.value = _coverage.value.copy(totalTypes = types.size, completedTypes = 0)
            val schemas = mutableSetOf<String>()
            fun routeFor(type: String) = types[type] ?: type.lowercase().replace(' ', '-')
            suspend fun downloadSchema(type: String): JSONObject {
                val route = routeFor(type)
                return store("schema:$route", "schema") { http.get(scope, "/api/v1/system/doctype/$route") } as JSONObject
            }
            val dependencies = ArrayDeque<Pair<String, Boolean>>()
            fun collectDependencies(schema: JSONObject) {
                val fields = schema.optJSONArray("fields") ?: schema.optJSONObject("doctype")?.optJSONArray("fields") ?: JSONArray()
                for (i in 0 until fields.length()) fields.optJSONObject(i)?.let { field ->
                    if (!field.optBoolean("hidden") && field.optString("fieldtype") in setOf("Table", "Link")) {
                        field.optString("options").takeIf(String::isNotBlank)?.let { dependencies.add(it to (field.optString("fieldtype") == "Table")) }
                    }
                }
            }
            suspend fun manifest(page: String) {
                try { store("page-manifest:$page", "navigation") { http.get(scope, "/api/v1/page-manifests", mapOf("route" to page)) } }
                catch (error: com.kora.mobile.domain.model.KoraError) { if (error.status != 404) throw error }
            }
            for ((name, route) in types) {
                val schema = downloadSchema(name)
                schemas += name
                collectDependencies(schema)
                val page = "/" + name.lowercase().replace(Regex("[^a-z0-9]+"), "-").trim('-')
                manifest(page)
                _coverage.value = _coverage.value.copy(completedTypes = _coverage.value.completedTypes + 1)
            }
            while (dependencies.isNotEmpty()) {
                val (child, isTable) = dependencies.removeFirst()
                if (!schemas.add(child)) continue
                val schema = downloadSchema(child)
                collectDependencies(schema)
                if (!isTable) types[child] = routeFor(child)
            }
            if (types.keys.any { it.equals("Product", true) }) manifest("/pos")
            network { localRecords.bootstrap(scope, types, generation) }
            val completed = System.currentTimeMillis()
            dao.putEntry(LocalEntry(scope.key, "offline-prepared", vault.encrypt(types.values.joinToString(",")), completed, "coverage"))
            _coverage.value = _coverage.value.copy(downloading = false, completedAt = completed)
        } catch (cancelled: CancellationException) { _coverage.value = _coverage.value.copy(downloading = false); throw cancelled }
        catch (error: Exception) { _coverage.value = _coverage.value.copy(downloading = false, error = error.message ?: "Download interrupted") }
    }

    suspend fun login(scope: Scope, email: String, password: String) {
        http.get(scope, "/api/auth/providers")
        http.post(scope, "/api/auth/login", JSONObject().put("email", email).put("password", password))
        http.get(scope, "/api/auth/me")
    }

    suspend fun currentUser(scope: Scope): JSONObject = network { http.get(scope, "/api/auth/me") as? JSONObject ?: JSONObject() }
    suspend fun logout(scope: Scope) { runCatching { http.post(scope, "/api/auth/logout") }; http.clearSession(scope) }
    suspend fun navigation(scope: Scope): JSONObject = cached(scope, "navigation", "navigation") { http.get(scope, "/api/v1/system/navigation") as? JSONObject ?: JSONObject() } as? JSONObject ?: JSONObject()
    suspend fun branding(scope: Scope): JSONObject = cached(scope, "tenant-branding", "navigation") {
        http.get(scope, "/api/v1/system/branding") as? JSONObject ?: JSONObject()
    } as? JSONObject ?: JSONObject()
    suspend fun pageManifest(scope: Scope, route: String): JSONObject = cached(scope, "page-manifest:$route", "navigation") {
        http.get(scope, "/api/v1/page-manifests", mapOf("route" to route)) as? JSONObject ?: JSONObject()
    } as? JSONObject ?: JSONObject()
    suspend fun executeViewAction(scope: Scope, actionId: String, view: String, component: String, context: JSONObject, idempotencyKey: String? = null): JSONObject {
        return http.post(scope, "/api/v1/view/action/$actionId", JSONObject()
            .put("view", view).put("component", component).put("context", context), idempotencyKey) as? JSONObject ?: JSONObject()
    }
    suspend fun records(scope: Scope, doctype: String): JSONArray {
        val local = localRecords.records(scope, doctype)
        if (localRecords.isCovered(scope, doctype)) {
            refresh("${scope.key}:pull") { localRecords.pull(scope) }
            return local
        }
        if (local.length() > 0 || dao.getEntry(scope.key, "records-downloaded:$doctype") != null || dao.getEntry(scope.key, "records:$doctype") != null) {
            refresh("${scope.key}:records:$doctype") { localRecords.refresh(scope, doctype) }
            return local
        }
        network { localRecords.refresh(scope, doctype) }
        return localRecords.records(scope, doctype)
    }
    fun observeRecords(scope: Scope, doctype: String): Flow<List<JSONObject>> = localRecords.observeRecords(scope, doctype)
    suspend fun record(scope: Scope, doctype: String, name: String): JSONObject {
        val local = localRecords.record(scope, doctype, name)
        if (localRecords.isCovered(scope, doctype)) {
            if (local != null) {
                refresh("${scope.key}:pull") { localRecords.pull(scope) }
                return local
            }
            network { localRecords.pull(scope) }
            return localRecords.record(scope, doctype, name) ?: throw IllegalStateException("Record not available on this device")
        }
        if (local != null) {
            refresh("${scope.key}:record:$doctype:$name") { localRecords.refreshRecord(scope, doctype, name) }
            return local
        }
        network { localRecords.refreshRecord(scope, doctype, name) }
        return localRecords.record(scope, doctype, name) ?: throw IllegalStateException("Record not available on this device")
    }
    suspend fun userDirectory(scope: Scope, names: List<String>): JSONArray {
        if (names.isEmpty()) return JSONArray()
        val requested = names.distinct().sorted()
        val fingerprint = java.security.MessageDigest.getInstance("SHA-256")
            .digest(requested.joinToString(",").toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
        val key = "user-directory:$fingerprint"
        val response = cached(scope, key, "users") {
            http.get(scope, "/api/v1/system/user-directory", mapOf("names" to requested.joinToString(",")))
        }
        return response as? JSONArray ?: JSONArray()
    }
    suspend fun schema(scope: Scope, doctype: String): JSONObject = cached(scope, "schema:$doctype", "schema") { http.get(scope, "/api/v1/system/doctype/$doctype") as? JSONObject ?: JSONObject() } as? JSONObject ?: JSONObject()
    suspend fun requests(scope: Scope): JSONObject = cached(scope, "requests", "requests") { http.get(scope, "/api/v2/requests", mapOf("view" to "needs_action", "kind" to "task", "limit" to "25", "overdue_only" to "false")) as? JSONObject ?: JSONObject() } as? JSONObject ?: JSONObject()

    suspend fun analyticsCatalog(scope: Scope): JSONObject = cached(scope, "analytics:catalog", "reports") {
        http.get(scope, "/api/v1/analytics/catalog") as? JSONObject ?: JSONObject()
    } as? JSONObject ?: JSONObject()

    suspend fun analyticsReports(scope: Scope): JSONArray = cached(scope, "analytics:reports", "reports") {
        http.get(scope, "/api/v1/analytics/reports") as? JSONArray ?: JSONArray()
    } as? JSONArray ?: JSONArray()

    suspend fun queryAnalytics(scope: Scope, request: JSONObject): JSONObject {
        val keyHash = java.security.MessageDigest.getInstance("SHA-256")
            .digest(request.toString().toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
        return cached(scope, "analytics:query:$keyHash", "reports") {
            http.post(scope, "/api/v1/analytics/query", request) as? JSONObject ?: JSONObject()
        } as? JSONObject ?: JSONObject()
    }

    suspend fun executeReport(scope: Scope, name: String, request: JSONObject): JSONObject {
        val keyHash = java.security.MessageDigest.getInstance("SHA-256")
            .digest(request.toString().toByteArray(Charsets.UTF_8))
            .joinToString("") { byte -> "%02x".format(byte.toInt() and 0xff) }
        return cached(scope, "analytics:report:$name:$keyHash", "reports") {
            http.post(scope, "/api/v1/analytics/reports/${java.net.URLEncoder.encode(name, "UTF-8")}/query", request) as? JSONObject ?: JSONObject()
        } as? JSONObject ?: JSONObject()
    }

    private suspend fun cached(scope: Scope, key: String, category: String, fetch: suspend () -> Any): Any {
        val local = dao.getEntry(scope.key, key)
        suspend fun fetchAndStore(): Any = network {
            fetch().also { dao.putEntry(LocalEntry(scope.key, key, vault.encrypt(it.toString()), System.currentTimeMillis(), category)) }
        }
        if (local != null) {
            val saved = vault.decrypt(local.encryptedPayload) ?: throw IllegalStateException("Saved data could not be decrypted")
            if (System.currentTimeMillis() - local.fetchedAt > 60_000) refresh("${scope.key}:$key") { fetchAndStore() }
            return JSONTokener(saved).nextValue()
        }
        return fetchAndStore()
    }

    // Legacy draft rows are removed when a workspace opens. Submitted offline records use the outbox.
    suspend fun clearDrafts(scope: Scope) = dao.clearDrafts(scope.key)
    suspend fun submitRecord(scope: Scope, doctype: String, recordName: String?, payload: JSONObject, idempotencyKey: String) {
        enqueueRecord(scope, doctype, recordName, payload, idempotencyKey, null)
    }

    fun queuedMutations(scope: Scope): Flow<List<LocalMutation>> = dao.observeMutations(scope.key).map { rows -> rows.map(::withDisplayName) }

    suspend fun enqueueRecord(scope: Scope, doctype: String, recordName: String?, payload: JSONObject, idempotencyKey: String, displayName: String?, draftId: String? = null) {
        localRecords.save(scope, doctype, recordName, payload, idempotencyKey, displayName, draftId ?: "$doctype:${recordName ?: "new"}")
    }

    private fun withDisplayName(mutation: LocalMutation): LocalMutation {
        if (!mutation.displayName.isNullOrBlank()) return mutation
        val payload = vault.decrypt(mutation.encryptedPayload)?.let { runCatching { JSONObject(it) }.getOrNull() } ?: return mutation
        val displayName = listOf("title", "product_name", "reference", "customer_name", "supplier_name", "name")
            .firstNotNullOfOrNull { key -> payload.optString(key).takeIf(String::isNotBlank) }
        return mutation.copy(displayName = displayName)
    }

    suspend fun copyMutation(mutation: LocalMutation, targetScopeKey: String) {
        dao.putMutation(mutation.copy(scopeKey = targetScopeKey))
    }
    suspend fun deleteMutation(scope: Scope, id: String) {
        dao.deleteMutation(scope.key, id)
    }
    suspend fun moveQueuedMutations(from: Scope, to: Scope): Int {
        if (from.key == to.key) return 0
        val rows = dao.queuedMutations(from.key)
        rows.forEach { dao.putMutation(it.copy(scopeKey = to.key)) }
        rows.forEach { dao.deleteMutation(from.key, it.id) }
        return rows.size
    }

    suspend fun enqueueViewAction(scope: Scope, actionId: String, request: JSONObject, idempotencyKey: String, recordType: String = "sale", projection: JSONObject? = null, effect: JSONObject? = null, draftId: String? = null) {
        val context = request.optJSONObject("context") ?: JSONObject()
        localRecords.saveAction(scope, actionId, request, idempotencyKey, recordType, projection ?: JSONObject(context.toString()).put("items", context.optJSONArray("cart") ?: JSONArray()), effect, draftId)
    }

    suspend fun syncQueuedRecords(scope: Scope, force: Boolean = false): Pair<Int, Exception?> {
        if (!canSync(scope)) return 0 to com.kora.mobile.domain.model.KoraError("actor_changed", "Sign in again to sync this workspace", 401)
        if (dao.queuedMutations(scope.key).isEmpty() && !localRecords.hasPullCursor(scope)) return 0 to null
        try {
            val user = currentUser(scope)
            if (user.optString("name") != scope.userId) return 0 to com.kora.mobile.domain.model.KoraError("actor_changed", "Sign in again to sync this workspace", 401)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (error: Exception) { return 0 to error }
        val result = localRecords.sync(scope, force)
        var pullError: Exception? = null
        if (localRecords.hasPullCursor(scope)) {
            try { network { localRecords.pull(scope) } }
            catch (cancelled: CancellationException) { throw cancelled }
            catch (error: Exception) { pullError = error }
        }
        if ((pullError as? com.kora.mobile.domain.model.KoraError)?.code == "offline.rebootstrap_required") {
            prepareOffline(scope)
            pullError = _coverage.value.error?.let { com.kora.mobile.domain.model.KoraError("offline.rebootstrap_required", it, 409) }
            if (pullError == null) {
                try { network { localRecords.pull(scope) } }
                catch (cancelled: CancellationException) { throw cancelled }
                catch (error: Exception) { pullError = error }
            }
        }
        if (result.error == null && pullError == null) _offline.value = false
        else if ((result.error as? com.kora.mobile.domain.model.KoraError)?.retryable == true) _offline.value = true
        // A queued backoff still needs durable work scheduled after the current worker exits.
        val error = if (result.retryAt != null) com.kora.mobile.domain.model.KoraError("retry_scheduled", "Saved changes will retry automatically", retryable = true) else result.error ?: pullError
        return result.acknowledged to error
    }
}
