package com.myapp.core.network.retry

data class RetryPolicy(
    val maxAttempts: Int = 3,
    val initialDelayMs: Long = 1_000L,
    val backoffFactor: Double = 2.0,
    val maxDelayMs: Long = 30_000L,
    val retryOn: (Throwable) -> Boolean = { true },
)
