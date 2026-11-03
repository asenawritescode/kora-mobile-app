package com.kora.mobile

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.kora.mobile.KoraApplication
import com.kora.mobile.domain.model.Scope

/** Revalidates the actor catalog and recovers durable E4 intents by their original idempotency keys. */
class OutboxSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as KoraApplication
        val scopeKey = inputData.getString("scope_key") ?: return Result.failure()
        val activeKey = app.getSharedPreferences("kora_session", 0).getString("active_scope", null)
        if (scopeKey != activeKey) return Result.success()
        val instance = app.instanceStore.current()
        val user = app.getSharedPreferences("kora_session", 0).getString("last_user", null) ?: return Result.success()
        val parts = scopeKey.split('|')
        if (parts.size != 6 || parts[0] != instance.origin.trimEnd('/') || parts[1] != instance.siteRef.orEmpty() || parts[3] != user) return Result.success()
        val businessScope = runCatching {
            val kind = parts[4]
            if (kind == "legal_entity") com.kora.mobile.domain.model.BusinessScope(parts[2], kind, parts[5])
            else com.kora.mobile.domain.model.BusinessScope(parts[2])
        }.getOrElse { return Result.failure() }
        val scope = Scope(instance.id, instance.origin, instance.siteRef, user, businessScope)
        return try {
            val userCheck = app.workspace.currentUser(scope).optString("name")
            if (userCheck != user) return Result.success()
            val catalog = app.workspace.publishedCatalog(scope)
            val result = app.workspace.syncPublishedIntents(scope, catalog)
            if (result.error == null) Result.success()
            else if (result.error is com.kora.mobile.domain.model.KoraError && (result.error as com.kora.mobile.domain.model.KoraError).status in 400..499) Result.success()
            else Result.retry()
        } catch (error: com.kora.mobile.domain.model.KoraError) { if (error.status in 400..499) Result.success() else Result.retry() }
        catch (_: java.io.IOException) { Result.retry() }
        catch (_: Exception) { Result.failure() }
    }
}
