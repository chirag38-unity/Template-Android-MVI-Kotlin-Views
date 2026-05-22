package com.myapp.core.common.concurrency

import com.myapp.core.common.result.Result
import kotlinx.coroutines.delay
import kotlin.math.min

suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelay: Long = 1000L,
    factor: Double = 2.0,
    maxDelay: Long = 30_000L,
    block: suspend () -> T,
): T {
    var currentDelay = initialDelay
    repeat(maxRetries) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            if (attempt == maxRetries - 1) throw e
        }
        delay(currentDelay)
        currentDelay = min((currentDelay * factor).toLong(), maxDelay)
    }
    // maxRetries <= 0: execute once and let any exception propagate
    return block()
}

suspend fun <T> retryUntilSuccess(
    maxAttempts: Int = 3,
    block: suspend () -> Result<T>,
): Result<T> {
    repeat(maxAttempts) {
        val result = block()
        if (result is Result.Success) return result
    }
    return block()
}
