package com.kora.mobile.data

import com.kora.mobile.core.database.LocalDao
import com.kora.mobile.core.database.LocalEntry
import com.kora.mobile.core.database.OfflineIntentEntity
import com.kora.mobile.core.database.KoraDatabase
import com.kora.mobile.data.offline.OfflineRecordRepository
import com.kora.mobile.data.offline.RestRecordDataSource
import com.kora.mobile.core.network.KoraHttpClient
import com.kora.mobile.core.contracts.EngineApplicationCatalog
import com.kora.mobile.core.contracts.EngineContractDecoder
import com.kora.mobile.core.contracts.EngineOperationReceipt
import com.kora.mobile.core.contracts.EnginePublishedOperation
import com.kora.mobile.core.contracts.EnginePublishedProjection
import com.kora.mobile.core.contracts.EnginePublishedQuery
import com.kora.mobile.core.contracts.EngineQueryPage
import com.kora.mobile.core.contracts.decodeApplicationCatalog
import com.kora.mobile.core.contracts.decodeOperationReceipt
import com.kora.mobile.core.contracts.decodeQueryPage
import com.kora.mobile.core.contracts.EngineResponseReceipt
import com.kora.mobile.core.contracts.EngineWorkflowInstance
import com.kora.mobile.core.contracts.EngineWorkflowDefinitionRef
import com.kora.mobile.core.contracts.decodeResponseReceipt
import com.kora.mobile.core.contracts.decodeWorkflowInstance
import com.kora.mobile.core.contracts.decodeWorkflowDefinitionRef
import com.kora.mobile.core.security.PayloadCipher
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.concurrent.ConcurrentHashMap
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener

class WorkspaceRepository(private val http: KoraHttpClient, private val db: KoraDatabase, private val vault: PayloadCipher, private val decoder: EngineContractDecoder, private val canSync: (Scope) -> Boolean = { true }) {
    private val dao = db.localDao
    val localRecords = OfflineRecordRepository(db, vault, RestRecordDataSource(http), decoder, canSync = canSync)
    private val background = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val refreshJobs = ConcurrentHashMap<String, Job>()
    private val _offline = MutableStateFlow(false)
    val offline: StateFlow<Boolean> = _offline
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
    fun bindAnonymousSession(from: Scope, to: Scope) = http.bindAnonymousSession(from, to)
    // Compatibility for old callers: cache use is no longer confused with connectivity.
    val lastResponseWasCached: Boolean get() = _offline.value

    suspend fun probe(scope: Scope) { http.get(scope, "/api/ping") }

    /** Actor-filtered capabilities are the only source for module-owned workspace surfaces. */
    suspend fun publishedCatalog(scope: Scope): EngineApplicationCatalog {
        val response = network { http.getResponse(scope, "/api/v2/modules/catalog") }
        val catalog = decoder.decodeApplicationCatalog(response.body.toString())
        require(catalog.businessScope == scope.businessScope) { "Engine catalog scope does not match the active workspace" }
        localRecords.rememberCatalog(scope, catalog)
        return catalog
    }

    suspend fun publishedQuery(scope: Scope, catalog: EngineApplicationCatalog, query: EnginePublishedQuery, input: Any, cursor: String? = null): EngineQueryPage {
        require(catalog.businessScope == scope.businessScope && catalog.queries.any { it.ownerModuleId == query.ownerModuleId && it.name == query.name && it.route == query.route }) {
            "Published query is not part of the active actor catalog"
        }
        validatePublishedValue(catalog, query.inputSchema, input)
        val request = JSONObject().put("input", input).put("limit", query.pageLimit)
        cursor?.takeIf(String::isNotBlank)?.let { request.put("cursor", it) }
        val response = network { http.postResponse(scope, query.route, request) }
        return decoder.decodeQueryPage(response.body.toString()).also { page -> validatePublishedValue(catalog, query.outputSchema, page.data) }
    }

    suspend fun publishedProjection(scope: Scope, catalog: EngineApplicationCatalog, projection: EnginePublishedProjection): EngineQueryPage {
        require(catalog.businessScope == scope.businessScope && catalog.projections.any { it.ownerModuleId == projection.ownerModuleId && it.id == projection.id && it.route == projection.route }) {
            "Published projection is not part of the active actor catalog"
        }
        val response = network { http.getResponse(scope, projection.route) }
        return decoder.decodeQueryPage(response.body.toString()).also { page -> validatePublishedValue(catalog, projection.outputSchema, page.data) }
    }

    /** Submission is online-only here; M3 owns durable offline intent queuing. */
    suspend fun submitPublishedOperation(scope: Scope, catalog: EngineApplicationCatalog, operation: EnginePublishedOperation, input: Any, idempotencyKey: String): EngineOperationReceipt {
        require(catalog.businessScope == scope.businessScope && catalog.operations.any { it.operationRef == operation.operationRef && it.inputSchema == operation.inputSchema }) {
            "Published operation is not part of the active actor catalog"
        }
        require(operation.availability == "available") { "This action is ${operation.availability}" }
        require(idempotencyKey.isNotBlank()) { "An idempotency key is required" }
        validatePublishedValue(catalog, operation.inputSchema, input)
        val request = JSONObject()
            .put("meta", JSONObject().put("idempotency_key", idempotencyKey))
            .put("input_schema", JSONObject().put("id", operation.inputSchema.id).put("version", operation.inputSchema.version))
            .put("input", input)
        val encodedRef = java.net.URLEncoder.encode(operation.operationRef, "UTF-8").replace("+", "%20")
        val response = network { http.postResponse(scope, "/api/v2/modules/operations/$encodedRef/execute", request, idempotencyKey) }
        return decoder.decodeOperationReceipt(response.body.toString()).also {
            require(it.businessScope == scope.businessScope && it.operationRef == operation.operationRef) { "Engine receipt identity does not match the submitted operation" }
            if (it.status == "completed") {
                require(it.outputSchema == operation.outputSchema) { "Engine receipt output schema does not match the published operation" }
                validatePublishedValue(catalog, operation.outputSchema, it.output ?: throw IllegalArgumentException("completed operation receipt has no output"))
            }
        }
    }

    suspend fun lookupPublishedOperation(scope: Scope, catalog: EngineApplicationCatalog, operation: EnginePublishedOperation, idempotencyKey: String): EngineOperationReceipt? {
        require(catalog.businessScope == scope.businessScope && catalog.operations.any { it.operationRef == operation.operationRef }) {
            "Published operation is not part of the active actor catalog"
        }
        require(idempotencyKey.isNotBlank()) { "An idempotency key is required" }
        val request = JSONObject().put("operation_ref", operation.operationRef).put("idempotency_key", idempotencyKey)
        return try {
            val response = network { http.postResponse(scope, "/api/v2/module-executions/lookup", request) }
            decoder.decodeOperationReceipt(response.body.toString()).also {
                require(it.businessScope == scope.businessScope && it.operationRef == operation.operationRef) { "Engine receipt identity does not match the lookup" }
                if (it.status == "completed") {
                    require(it.outputSchema == operation.outputSchema) { "Engine receipt output schema does not match the published operation" }
                    validatePublishedValue(catalog, operation.outputSchema, it.output ?: throw IllegalArgumentException("completed operation receipt has no output"))
                }
            }
        } catch (error: com.kora.mobile.domain.model.KoraError) {
            if (error.status == 404 && error.code == "module.operation_receipt_not_found") null else throw error
        }
    }

    suspend fun queuePublishedIntent(scope: Scope, catalog: EngineApplicationCatalog, operation: EnginePublishedOperation, input: Any, idempotencyKey: String): String =
        enqueuePublishedIntent(scope, catalog, operation, input, idempotencyKey)

    suspend fun syncPublishedIntents(scope: Scope, catalog: EngineApplicationCatalog) = localRecords.sync(scope, catalog)
    suspend fun syncPublishedIntents(scope: Scope) = localRecords.observeCatalogThenSync(scope)
    suspend fun bootstrapPublishedFeed(scope: Scope, catalog: EngineApplicationCatalog, query: EnginePublishedQuery, input: Any, limit: Int? = null): JSONObject =
        localRecords.bootstrap(scope, catalog, query, input, limit)
    suspend fun pullPublishedFeed(scope: Scope, catalog: EngineApplicationCatalog, query: EnginePublishedQuery, input: Any, checkpoint: JSONObject, limit: Int = 100): JSONObject =
        localRecords.pull(scope, catalog, query, input, checkpoint, limit)
    suspend fun savedPublishedFeedCheckpoint(scope: Scope, catalog: EngineApplicationCatalog, query: EnginePublishedQuery, input: Any): JSONObject? =
        localRecords.savedCheckpoint(scope, catalog, query, input)
    suspend fun hasPendingPublishedIntents(scope: Scope): Boolean = localRecords.hasPending(scope)
    fun observePublishedIntents(scope: Scope): Flow<List<OfflineIntentEntity>> = localRecords.observeIntents(scope)

    private fun validatePublishedValue(catalog: EngineApplicationCatalog, ref: com.kora.mobile.core.contracts.EngineSchemaRef, value: Any) {
        val document = catalog.schemas.singleOrNull { it.ref == ref }?.schema
            ?: throw IllegalArgumentException("The actor catalog did not publish schema ${ref.id} v${ref.version}")
        val violations = decoder.validateSchemaDocument(document, value)
        if (violations.isNotEmpty()) throw IllegalArgumentException("Published value failed ${ref.id} v${ref.version}: ${violations.joinToString { "${it.path}: ${it.message}" }}")
    }

    suspend fun requestMagicLink(scope: Scope, email: String) {
        require(email.isNotBlank()) { "Enter your email address" }
        // This endpoint is anonymous and rate limited. A bearer header selects
        // the Engine API client's documented CSRF path; it does not authenticate
        // or grant access to the account.
        val requestMarker = java.util.UUID.randomUUID().toString()
        http.post(scope, "/api/auth/magic-link/request", JSONObject().put("email", email.trim()), authorization = "Bearer $requestMarker")
    }

    suspend fun verifyMagicLink(scope: Scope, token: String): JSONObject {
        require(token.isNotBlank())
        http.post(scope, "/api/auth/magic-link/verify", JSONObject().put("token", token), authorization = "Bearer $token")
        return currentUser(scope)
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
    suspend fun records(scope: Scope, doctype: String): JSONArray {
        throw IllegalStateException("Legacy DocType records are unavailable. Load a published module query instead.")
    }
    fun observeRecords(scope: Scope, doctype: String): Flow<List<JSONObject>> = kotlinx.coroutines.flow.flowOf(emptyList())
    suspend fun record(scope: Scope, doctype: String, name: String): JSONObject {
        throw IllegalStateException("Legacy DocType records are unavailable. Load a published module query instead.")
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
    suspend fun requests(scope: Scope): JSONObject = cached(scope, "requests", "requests") { http.get(scope, "/api/v2/requests", mapOf("view" to "needs_action", "limit" to "25", "overdue_only" to "false")) as? JSONObject ?: JSONObject() } as? JSONObject ?: JSONObject()

    suspend fun respondToRequest(scope: Scope, requestId: String, assignmentId: String, expectedRevision: String, response: String, comment: String = "", idempotencyKey: String): EngineResponseReceipt {
        require(idempotencyKey.isNotBlank() && expectedRevision.isNotBlank())
        val body = JSONObject().put("meta", JSONObject().put("idempotency_key", idempotencyKey).put("expected_revision", expectedRevision))
            .put("assignment_id", assignmentId).put("response", response).put("comment", comment)
        val result = network { http.postResponse(scope, "/api/v2/requests/${pathSegment(requestId)}/responses", body, idempotencyKey) }
        return decoder.decodeResponseReceipt(result.body.toString())
    }

    suspend fun workflowInstances(scope: Scope): List<EngineWorkflowInstance> {
        val result = network { http.getResponse(scope, "/api/v2/workflow-instances") }
        val rows = when (val body = result.body) {
            is JSONArray -> body
            is JSONObject -> body.optJSONArray("items") ?: JSONArray()
            else -> JSONArray()
        }
        return (0 until rows.length()).map { decoder.decodeWorkflowInstance(rows.getJSONObject(it).toString()) }
    }

    suspend fun workflowDefinitions(scope: Scope): List<EngineWorkflowDefinitionRef> {
        val result = network { http.getResponse(scope, "/api/v2/workflow-definitions") }
        val rows = result.body as? JSONArray ?: JSONArray()
        return (0 until rows.length()).map { decoder.decodeWorkflowDefinitionRef(rows.getJSONObject(it).toString()) }
    }

    suspend fun startWorkflow(scope: Scope, definitionId: String, version: Int, subjectType: String, subjectId: String, idempotencyKey: String): EngineWorkflowInstance {
        require(definitionId.isNotBlank() && version > 0 && subjectType.isNotBlank() && subjectId.isNotBlank() && idempotencyKey.isNotBlank())
        val body = JSONObject().put("meta", JSONObject().put("idempotency_key", idempotencyKey))
            .put("definition_id", definitionId).put("definition_version", version)
            .put("subject", JSONObject().put("kind", subjectType).put("id", subjectId))
            .put("business_scope", JSONObject().put("tenant_id", scope.businessScope.tenantId).put("kind", scope.businessScope.kind).apply {
                scope.businessScope.legalEntityId?.let { put("legal_entity_id", it) }
            })
        val result = network { http.postResponse(scope, "/api/v2/workflow-instances", body, idempotencyKey) }
        return decoder.decodeWorkflowInstance(result.body.toString())
    }

    suspend fun workflowHistory(scope: Scope, id: String): JSONArray =
        (network { http.getResponse(scope, "/api/v2/workflow-instances/${pathSegment(id)}/history") }.body as? JSONArray) ?: JSONArray()

    suspend fun reconcileWorkflow(scope: Scope, id: String, expectedRevision: String, stepId: String, outcome: String, evidenceRef: String, reason: String, idempotencyKey: String): EngineWorkflowInstance {
        require(idempotencyKey.isNotBlank() && expectedRevision.isNotBlank() && evidenceRef.isNotBlank() && reason.isNotBlank())
        val body = JSONObject().put("meta", JSONObject().put("idempotency_key", idempotencyKey).put("expected_revision", expectedRevision))
            .put("step_id", stepId).put("outcome", outcome).put("evidence_ref", evidenceRef).put("reason", reason)
        val result = network { http.postResponse(scope, "/api/v2/workflow-instances/${pathSegment(id)}/reconcile", body, idempotencyKey) }
        return decoder.decodeWorkflowInstance(result.body.toString())
    }

    suspend fun cancelWorkflow(scope: Scope, id: String, expectedRevision: String, reason: String, idempotencyKey: String): EngineWorkflowInstance {
        require(idempotencyKey.isNotBlank() && expectedRevision.isNotBlank() && reason.isNotBlank())
        val body = JSONObject().put("meta", JSONObject().put("idempotency_key", idempotencyKey).put("expected_revision", expectedRevision)).put("reason", reason)
        val result = network { http.postResponse(scope, "/api/v2/workflow-instances/${pathSegment(id)}/cancel", body, idempotencyKey) }
        return decoder.decodeWorkflowInstance(result.body.toString())
    }

    private fun pathSegment(value: String) = java.net.URLEncoder.encode(value, "UTF-8").replace("+", "%20")

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

    suspend fun enqueuePublishedIntent(scope: Scope, catalog: EngineApplicationCatalog, operation: EnginePublishedOperation, input: Any, idempotencyKey: String, expectedRevision: String? = null): String =
        localRecords.enqueue(scope, catalog, operation, input, idempotencyKey, expectedRevision)

}
