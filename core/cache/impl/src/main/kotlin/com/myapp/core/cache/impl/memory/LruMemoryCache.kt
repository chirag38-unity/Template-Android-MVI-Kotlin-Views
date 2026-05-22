package com.myapp.core.cache.impl.memory

import com.myapp.core.cache.api.Cache
import com.myapp.core.cache.api.CacheKey
import com.myapp.core.cache.api.CachedValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.util.concurrent.ConcurrentHashMap

/**
 * An in-memory [Cache] backed by a LinkedHashMap configured for LRU eviction.
 *
 * Type safety contract: entries are stored as `Any` internally and cast back to `T` using the
 * type guarantee provided by [CacheKey].  The caller is responsible for using distinct key strings
 * for distinct value types — the same guarantee required by any heterogeneous map.
 *
 * @param maxSize     maximum number of entries before the least-recently-used entry is evicted
 * @param onEviction  optional callback invoked with (keyString, evicted value) after eviction
 * @param diskCache   optional persistent [Cache] used as a fallback when a key is not in memory;
 *                    successful fallback reads are promoted into the memory cache automatically
 */
class LruMemoryCache(
    private val maxSize: Int = DEFAULT_MAX_SIZE,
    private val onEviction: ((String, Any) -> Unit)? = null,
    private val diskCache: Cache? = null,
) : Cache {

    // All lruMap accesses use synchronized(lruMap) so that both the non-suspending getSync() and
    // the suspending put/get/remove/clear operations share a single consistent lock.  Operations
    // inside the lock are O(1) and never suspend, so blocking the thread briefly is acceptable.
    private val lruMap: LinkedHashMap<String, CachedValue<*>> =
        object : LinkedHashMap<String, CachedValue<*>>(maxSize, LOAD_FACTOR, true) {
            override fun removeEldestEntry(
                eldest: Map.Entry<String, CachedValue<*>>?,
            ): Boolean {
                val shouldEvict = size > maxSize
                if (shouldEvict && eldest != null) {
                    onEviction?.invoke(eldest.key, eldest.value.data)
                }
                return shouldEvict
            }
        }

    // One StateFlow per key for reactive observation.  ConcurrentHashMap guarantees thread-safe
    // reads/writes across both suspend and non-suspend (observe) callers.
    private val flows = ConcurrentHashMap<String, MutableStateFlow<CachedValue<*>?>>()

    // -------------------------------------------------------------------------------------------
    // Cache interface
    // -------------------------------------------------------------------------------------------

    override suspend fun <T : Any> put(key: CacheKey<T>, value: T) {
        val now = System.currentTimeMillis()
        val expiresAt = key.ttl?.let { now + it.inWholeMilliseconds }
        val entry = CachedValue(data = value, createdAt = now, expiresAt = expiresAt, version = key.version)
        synchronized(lruMap) {
            lruMap[key.key] = entry
            getOrCreateFlow(key.key).value = entry
        }
    }

    override suspend fun <T : Any> get(key: CacheKey<T>): CachedValue<T>? {
        val inMemory = synchronized(lruMap) { lruMap[key.key] }
        if (inMemory != null) {
            if (inMemory.isExpired()) {
                remove(key)
                return diskFallback(key)
            }
            @Suppress("UNCHECKED_CAST")
            return inMemory as CachedValue<T>
        }
        return diskFallback(key)
    }

    override suspend fun <T : Any> remove(key: CacheKey<T>) {
        synchronized(lruMap) {
            lruMap.remove(key.key)
            flows[key.key]?.value = null
        }
    }

    override suspend fun clear() {
        synchronized(lruMap) {
            lruMap.clear()
            flows.values.forEach { it.value = null }
        }
    }

    override fun <T : Any> observe(key: CacheKey<T>): Flow<CachedValue<T>?> {
        val flow = flows.computeIfAbsent(key.key) { MutableStateFlow(null) }
        @Suppress("UNCHECKED_CAST")
        return flow.asStateFlow().map { it as? CachedValue<T> }
    }

    /**
     * Synchronous read directly from the LRU map — no suspension needed.
     * Returns `null` for expired or absent entries without touching the disk.
     */
    override fun <T : Any> getSync(key: CacheKey<T>): CachedValue<T>? {
        val entry = synchronized(lruMap) { lruMap[key.key] } ?: return null
        if (entry.isExpired()) return null
        @Suppress("UNCHECKED_CAST")
        return entry as? CachedValue<T>
    }

    // -------------------------------------------------------------------------------------------
    // Extended API
    // -------------------------------------------------------------------------------------------

    /**
     * Populates the cache with the given [entries] without evicting existing items beyond the
     * configured [maxSize] limit (LRU eviction still applies to the map itself).
     */
    suspend fun warmUp(entries: Map<CacheKey<*>, Any>) {
        entries.forEach { (key, value) ->
            @Suppress("UNCHECKED_CAST")
            putUnchecked(key as CacheKey<Any>, value)
        }
    }

    /**
     * Removes all entries whose key string satisfies [predicate].
     */
    suspend fun invalidate(predicate: (String) -> Boolean) {
        synchronized(lruMap) {
            val toRemove = lruMap.keys.filter(predicate)
            toRemove.forEach { keyStr ->
                lruMap.remove(keyStr)
                flows[keyStr]?.value = null
            }
        }
    }

    // -------------------------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------------------------

    private suspend fun <T : Any> diskFallback(key: CacheKey<T>): CachedValue<T>? {
        val disk = diskCache ?: return null
        val entry = disk.get(key) ?: return null
        synchronized(lruMap) {
            lruMap[key.key] = entry
            getOrCreateFlow(key.key).value = entry
        }
        return entry
    }

    @Suppress("UNCHECKED_CAST")
    private suspend fun putUnchecked(key: CacheKey<Any>, value: Any) {
        put(key, value)
    }

    private fun getOrCreateFlow(keyStr: String): MutableStateFlow<CachedValue<*>?> {
        return flows.computeIfAbsent(keyStr) { MutableStateFlow(lruMap[keyStr]) }
    }

    companion object {
        private const val DEFAULT_MAX_SIZE = 64
        private const val LOAD_FACTOR = 0.75f
    }
}
