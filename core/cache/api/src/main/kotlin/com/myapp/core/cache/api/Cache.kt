package com.myapp.core.cache.api

import kotlinx.coroutines.flow.Flow

/**
 * Generic type-safe cache interface.
 *
 * Type safety is enforced through [CacheKey]: the [KSerializer] embedded in the key is used for
 * serialisation/deserialisation, so no unsafe casts are needed at the call-site.
 *
 * Implementations are expected to be thread-safe.
 */
interface Cache {

    /**
     * Stores [value] under [key].  Any existing entry for the same key is replaced.
     * If [CacheKey.ttl] is set the entry will be treated as expired after that duration.
     */
    suspend fun <T : Any> put(key: CacheKey<T>, value: T)

    /**
     * Returns the cached [CachedValue] for [key], or `null` if no valid (non-expired) entry
     * exists.  Expired entries are removed lazily on access.
     */
    suspend fun <T : Any> get(key: CacheKey<T>): CachedValue<T>?

    /** Removes the entry associated with [key]. */
    suspend fun <T : Any> remove(key: CacheKey<T>)

    /** Removes all entries from the cache. */
    suspend fun clear()

    /**
     * Returns a [Flow] that emits the current [CachedValue] for [key] and re-emits whenever the
     * stored value changes.  Emits `null` when no valid entry exists.
     */
    fun <T : Any> observe(key: CacheKey<T>): Flow<CachedValue<T>?>

    /**
     * Synchronous, non-suspending read intended for memory caches.
     * Disk-backed implementations should return `null`; callers must not rely on this returning
     * a value for persistent caches.
     */
    fun <T : Any> getSync(key: CacheKey<T>): CachedValue<T>? = null
}
