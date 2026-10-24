package com.kora.mobile

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.kora.mobile.domain.model.KoraError
import com.kora.mobile.domain.model.Scope
import java.util.concurrent.TimeUnit

class OutboxSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val app = applicationContext as? KoraApplication ?: return Result.failure()
        val scope = Scope(
            inputData.getString("instance") ?: return Result.failure(),
            inputData.getString("origin") ?: return Result.failure(),
            inputData.getString("site"),
            inputData.getString("actor") ?: return Result.failure(),
        )
        if (!app.isActiveScope(scope)) return Result.success()

        while (!isStopped) {
            val (synced, error) = app.workspace.syncQueuedRecords(scope)
            if (error != null) {
                return if ((error as? KoraError)?.retryable == true) Result.retry()
                else Result.failure(Data.Builder().putString("error", error.message).build())
            }
            if (synced == 0) return Result.success()
        }
        return Result.retry()
    }

    companion object {
        fun schedule(context: Context, scope: Scope) {
            val request = OneTimeWorkRequestBuilder<OutboxSyncWorker>()
                .setInputData(Data.Builder().putString("instance", scope.instanceId).putString("origin", scope.origin)
                    .putString("site", scope.siteRef).putString("actor", scope.userId).build())
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "kora-outbox-${scope.key}",
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request,
            )
        }
    }
}
