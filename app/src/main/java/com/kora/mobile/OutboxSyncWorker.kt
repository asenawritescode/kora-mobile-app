package com.kora.mobile

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

/** Retired record-only jobs terminate without replay; M3 replaces them with scoped domain intents. */
class OutboxSyncWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result = Result.success()
}
