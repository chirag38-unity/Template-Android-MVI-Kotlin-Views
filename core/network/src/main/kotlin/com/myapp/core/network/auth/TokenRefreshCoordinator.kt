package com.myapp.core.network.auth

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject

class TokenRefreshCoordinator @Inject constructor(
    private val authenticator: Authenticator,
) {
    private val mutex = Mutex()
    private var lastRefreshTime = 0L

    suspend fun refreshIfNeeded(): Boolean = mutex.withLock {
        val now = System.currentTimeMillis()
        if (now - lastRefreshTime < MIN_REFRESH_INTERVAL_MS) return@withLock true
        val success = authenticator.refreshToken()
        if (success) lastRefreshTime = now
        success
    }

    companion object {
        private const val MIN_REFRESH_INTERVAL_MS = 5_000L
    }
}
