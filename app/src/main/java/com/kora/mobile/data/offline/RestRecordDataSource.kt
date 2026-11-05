package com.kora.mobile.data.offline

import com.kora.mobile.core.network.KoraHttpClient
import com.kora.mobile.domain.model.KoraError
import com.kora.mobile.domain.model.Scope
import org.json.JSONObject

/** E4 transport. No generic resource writes or view-action replay exist on this path. */
class RestRecordDataSource(private val http: KoraHttpClient) {
    suspend fun capabilities(scope: Scope): JSONObject = http.get(scope, "/api/v2/offline/capabilities") as? JSONObject
        ?: throw KoraError("invalid_capabilities", "Engine returned invalid offline capabilities")

    suspend fun sendIntent(scope: Scope, intent: JSONObject): JSONObject = http.post(scope, "/api/v2/offline/intents", JSONObject().put("intents", org.json.JSONArray().put(intent))) as? JSONObject
        ?: throw KoraError("invalid_receipt", "Engine returned an invalid offline intent receipt", retryable = true)

    suspend fun lookupIntent(scope: Scope, operationRef: String, idempotencyKey: String): JSONObject? = try {
        http.post(scope, "/api/v2/module-executions/lookup", JSONObject().put("operation_ref", operationRef).put("idempotency_key", idempotencyKey)) as? JSONObject
            ?: throw KoraError("invalid_receipt", "Engine returned an invalid operation receipt", retryable = true)
    } catch (error: KoraError) {
        if (error.status == 404 && error.code == "module.operation_receipt_not_found") null else throw error
    }

    suspend fun bootstrap(scope: Scope, request: JSONObject): JSONObject = http.post(scope, "/api/v2/offline/bootstrap", request) as? JSONObject
        ?: throw KoraError("invalid_snapshot", "Engine returned an invalid offline snapshot", retryable = true)

    suspend fun pull(scope: Scope, request: JSONObject): JSONObject = http.post(scope, "/api/v2/offline/pull", request) as? JSONObject
        ?: throw KoraError("invalid_feed", "Engine returned an invalid offline feed", retryable = true)
}
