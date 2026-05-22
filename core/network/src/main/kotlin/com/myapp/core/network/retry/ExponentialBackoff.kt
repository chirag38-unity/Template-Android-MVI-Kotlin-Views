package com.myapp.core.network.retry

import kotlinx.coroutines.delay
import kotlin.math.min

suspend fun <T> executeWithRetry(
    policy: RetryPolicy = RetryPolicy(),
    block: suspend () -> T,
): T {
    var currentDelay = policy.initialDelayMs
    var lastException: Throwable? = null

    repeat(policy.maxAttempts) { attempt ->
        try {
            return block()
        } catch (e: Throwable) {
            lastException = e
            if (!policy.retryOn(e) || attempt == policy.maxAttempts - 1) throw e
            delay(currentDelay)
            currentDelay = min((currentDelay * policy.backoffFactor).toLong(), policy.maxDelayMs)
        }
    }
    throw lastException ?: IllegalStateException("Retry exhausted")
}
