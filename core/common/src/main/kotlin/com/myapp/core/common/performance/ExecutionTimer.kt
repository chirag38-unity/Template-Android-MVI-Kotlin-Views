package com.myapp.core.common.performance

import timber.log.Timber
import kotlin.system.measureTimeMillis

object ExecutionTimer {

    fun <T> measure(tag: String, block: () -> T): T {
        var result: T? = null
        val elapsed = measureTimeMillis { result = block() }
        Timber.d("[$tag] executed in ${elapsed}ms")
        @Suppress("UNCHECKED_CAST")
        return result as T
    }

    suspend fun <T> measureSuspend(tag: String, block: suspend () -> T): T {
        val start = System.currentTimeMillis()
        val result = block()
        val elapsed = System.currentTimeMillis() - start
        Timber.d("[$tag] executed in ${elapsed}ms")
        return result
    }
}
