package com.kora.mobile.data.offline

data class SyncResult(val acknowledged: Int, val error: Exception?, val retryAt: Long? = null)
