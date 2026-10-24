package com.kora.mobile.data.offline

import com.kora.mobile.core.database.LocalMutation
import com.kora.mobile.domain.model.Scope
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

/** The compatibility transport uses existing Kora resource/kernel endpoints. */
interface RecordRemoteDataSource {
    suspend fun capabilities(scope: Scope): JSONObject
    suspend fun list(scope: Scope, doctype: String, offset: Int, limit: Int): JSONArray
    suspend fun get(scope: Scope, doctype: String, name: String): JSONObject
    suspend fun send(scope: Scope, operation: LocalMutation, serverId: String?, payload: JSONObject): JSONObject
    suspend fun bootstrap(scope: Scope, doctypes: List<String>, snapshotToken: String? = null, pageToken: String? = null): JSONObject
    suspend fun pull(scope: Scope, cursor: String): JSONObject
}

interface RecordRepository {
    fun observeRecords(scope: Scope, doctype: String): Flow<List<JSONObject>>
    suspend fun records(scope: Scope, doctype: String): JSONArray
    suspend fun record(scope: Scope, doctype: String, name: String): JSONObject?
    suspend fun save(scope: Scope, doctype: String, name: String?, payload: JSONObject, idempotencyKey: String, displayName: String?, draftId: String? = null): String
    suspend fun refresh(scope: Scope, doctype: String)
}

interface SyncCoordinator {
    suspend fun sync(scope: Scope, force: Boolean = false): SyncResult
}

data class SyncResult(val acknowledged: Int, val error: Exception?, val retryAt: Long? = null)

data class OfflineCoverage(val downloading: Boolean = false, val completedTypes: Int = 0, val totalTypes: Int = 0, val completedAt: Long? = null, val error: String? = null)
