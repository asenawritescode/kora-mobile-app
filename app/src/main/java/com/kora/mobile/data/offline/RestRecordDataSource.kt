package com.kora.mobile.data.offline

import com.kora.mobile.core.database.LocalMutation
import com.kora.mobile.core.network.KoraHttpClient
import com.kora.mobile.domain.model.KoraError
import com.kora.mobile.domain.model.Scope
import org.json.JSONArray
import org.json.JSONObject

class RestRecordDataSource(private val http: KoraHttpClient) : RecordRemoteDataSource {
    override suspend fun capabilities(scope: Scope): JSONObject =
        http.get(scope, "/api/v1/offline/capabilities") as? JSONObject
            ?: throw KoraError("invalid_capabilities", "Kora returned invalid offline capabilities", retryable = true)
    override suspend fun bootstrap(scope: Scope, doctypes: List<String>, snapshotToken: String?, pageToken: String?): JSONObject {
        val request = JSONObject().put("limit", 100)
        if (snapshotToken == null) request.put("doctypes", JSONArray(doctypes))
        else request.put("snapshot_token", snapshotToken).put("page_token", pageToken ?: "")
        return http.post(scope, "/api/v1/offline/bootstrap", request) as? JSONObject
            ?: throw KoraError("invalid_bootstrap", "Kora returned an invalid workspace page", retryable = true)
    }

    override suspend fun pull(scope: Scope, cursor: String): JSONObject =
        http.post(scope, "/api/v1/offline/pull", JSONObject().put("cursor", cursor).put("limit", 100)) as? JSONObject
            ?: throw KoraError("invalid_pull", "Kora returned an invalid change page", retryable = true)

    override suspend fun list(scope: Scope, doctype: String, offset: Int, limit: Int): JSONArray {
        return when (val result = http.get(scope, "/api/v1/resource/$doctype", mapOf("limit" to "$limit", "offset" to "$offset", "order_by" to "name asc"))) {
            is JSONArray -> result
            is JSONObject -> result.optJSONArray("items") ?: throw KoraError("invalid_list", "Kora returned an unexpected record list")
            else -> throw KoraError("invalid_list", "Kora returned an unexpected record list")
        }
    }

    override suspend fun get(scope: Scope, doctype: String, name: String): JSONObject =
        http.get(scope, "/api/v1/resource/$doctype/$name") as? JSONObject ?: throw KoraError("invalid_record", "Kora returned an unexpected record")

    override suspend fun send(scope: Scope, operation: LocalMutation, serverId: String?, payload: JSONObject): JSONObject {
        if (operation.doctype.startsWith("@view-action/")) {
            return http.post(scope, "/api/v1/view/action/${operation.doctype.removePrefix("@view-action/")}", payload, operation.idempotencyKey) as? JSONObject
                ?: throw KoraError("invalid_receipt", "Kora did not return a saved sale", retryable = true)
        }
        // Existing v3 operations already attempted /resource. Preserve their command digest.
        if (operation.localId == null) {
            val path = "/api/v1/resource/${operation.doctype}" + (serverId?.let { "/$it" } ?: "")
            return (if (serverId == null) http.post(scope, path, payload, operation.idempotencyKey) else http.put(scope, path, payload, operation.idempotencyKey)) as? JSONObject
                ?: throw KoraError("invalid_receipt", "Kora did not return a saved record", retryable = true)
        }
        val command = if (serverId == null) "record.create" else "record.update"
        if (serverId != null && operation.baseRevision.isNullOrBlank()) throw KoraError("version_unavailable", "Download this record from an updated Kora server before syncing an edit. Your change is still saved on this device.", 409)
        val wire = JSONObject().put("id", operation.id).put("idempotency_key", operation.idempotencyKey)
            .put("command", command).put("doctype", operation.doctype).put("payload", payload)
        serverId?.let { wire.put("name", it) }
        operation.baseRevision?.let { wire.put("expected_revision", it) }
        val response = http.post(scope, "/api/v1/offline/push", JSONObject().put("operations", JSONArray().put(wire))) as? JSONObject
        val receipts = response?.optJSONArray("receipts")
        val receipt = receipts?.optJSONObject(0)
        if (receipts?.length() != 1 || receipt?.optString("operation_id") != operation.id) {
            throw KoraError("invalid_receipt", "Kora did not confirm this saved operation", retryable = true)
        }
        return when (receipt.optString("status")) {
            "acked" -> receipt.optJSONObject("record") ?: throw KoraError("invalid_receipt", "Kora did not return a saved record", retryable = true)
            "conflict", "rejected" -> {
                val problem = receipt.optJSONObject("error")
                throw KoraError(problem?.optString("code")?.takeIf(String::isNotBlank) ?: "sync_rejected",
                    problem?.optString("message")?.takeIf(String::isNotBlank) ?: "Kora could not save this record",
                    if (receipt.optString("status") == "conflict") 409 else 400)
            }
            else -> {
                val problem = receipt.optJSONObject("error")
                if (problem?.optString("code") == "UNAUTHENTICATED") throw KoraError("unauthenticated", "Sign in again to sync this workspace", 401)
                throw KoraError("sync_deferred", problem?.optString("message")?.takeIf(String::isNotBlank)
                    ?: "Kora has not confirmed this record yet", retryable = true)
            }
        }
    }
}
