package com.myapp.core.network.dedup

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Deduplicates concurrent requests with the same [key] so that only one in-flight coroutine
 * executes [block] at a time. All callers sharing the same key await the same [Deferred].
 *
 * **Failure behaviour:** if [block] throws, the exception propagates to every awaiting caller.
 * The key is removed from the in-flight map on completion (success *or* failure), so the next
 * call with the same key will start a fresh execution — failed requests are not cached.
 */
class RequestDeduplicator<K, V> {

    private val mutex = Mutex()
    private val inFlight = mutableMapOf<K, Deferred<V>>()

    suspend fun execute(
        key: K,
        block: suspend () -> V,
    ): V = coroutineScope {
        val deferred = mutex.withLock {
            inFlight.getOrPut(key) {
                async {
                    try {
                        block()
                    } finally {
                        mutex.withLock {
                            inFlight.remove(key)
                        }
                    }
                }
            }
        }
        deferred.await()
    }
}
