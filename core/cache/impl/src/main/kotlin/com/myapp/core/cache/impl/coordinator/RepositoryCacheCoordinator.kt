package com.myapp.core.cache.impl.coordinator

import com.myapp.core.cache.api.Cache
import com.myapp.core.cache.api.CacheKey
import com.myapp.core.cache.api.CachePolicy
import com.myapp.core.cache.api.CachedValue
import com.myapp.core.cache.impl.memory.LruMemoryCache
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import timber.log.Timber

/**
 * Coordinates reads and writes across an in-memory [LruMemoryCache] and a persistent [Cache]
 * (e.g. [DataStoreCache][com.myapp.core.cache.impl.datastore.DataStoreCache]) according to a
 * configurable [CachePolicy].
 *
 * Typical usage (inside a repository):
 * ```kotlin
 * class PlayerRepository @Inject constructor(
 *     memoryCache : LruMemoryCache,
 *     diskCache   : Cache,
 *     api         : PlayerApi,
 *     scope       : CoroutineScope,
 * ) {
 *     private val coordinator = RepositoryCacheCoordinator(
 *         key          = CacheKey("players", ListSerializer(Player.serializer()), ttl = 5.minutes),
 *         memoryCache  = memoryCache,
 *         diskCache    = diskCache,
 *         policy       = CachePolicy.CACHE_FIRST,
 *         networkFetch = { api.getPlayers() },
 *         scope        = scope,
 *     )
 *
 *     fun observe(): Flow<List<Player>> = coordinator.observe()
 *     suspend fun refresh()             = coordinator.refresh()
 * }
 * ```
 *
 * @param T             the type of data managed by this coordinator
 * @param key           [CacheKey] identifying the cache entry
 * @param memoryCache   in-memory LRU cache (fast, volatile)
 * @param diskCache     persistent cache (slower, survives process death)
 * @param policy        resolution strategy; defaults to [CachePolicy.CACHE_FIRST]
 * @param networkFetch  suspending lambda that fetches fresh data from the network
 * @param scope         [CoroutineScope] used for background revalidation in
 *                      [CachePolicy.STALE_WHILE_REVALIDATE]
 */
class RepositoryCacheCoordinator<T : Any>(
    private val key: CacheKey<T>,
    private val memoryCache: LruMemoryCache,
    private val diskCache: Cache,
    private val policy: CachePolicy = CachePolicy.CACHE_FIRST,
    private val networkFetch: suspend () -> T,
    private val scope: CoroutineScope,
) {

    // -------------------------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------------------------

    /**
     * Resolves a value according to [policy] and returns the unwrapped data, or `null` if no
     * value could be obtained.
     */
    suspend fun get(): T? = when (policy) {
        CachePolicy.CACHE_FIRST -> getCacheFirst()
        CachePolicy.NETWORK_FIRST -> getNetworkFirst()
        CachePolicy.MEMORY_ONLY -> getMemoryOnly()
        CachePolicy.DISK_ONLY -> getDiskOnly()
        CachePolicy.STALE_WHILE_REVALIDATE -> getStaleWhileRevalidate()
    }

    /**
     * Forces a fresh network fetch and updates both caches regardless of [policy].
     * Useful for explicit pull-to-refresh flows.
     */
    suspend fun refresh(): T? = fetchAndStore()

    /**
     * Returns a [Flow] that emits whenever the cached value changes.
     *
     * For [CachePolicy.STALE_WHILE_REVALIDATE] the flow emits the stale value first (if any),
     * then the fresh value once the background revalidation completes.
     */
    fun observe(): Flow<T> = channelFlow {
        // Emit cached values from disk as the primary source of truth.
        launch {
            diskCache.observe(key).filterNotNull().collect { cached ->
                if (!cached.isExpired()) send(cached.data)
            }
        }

        // Kick off initial resolution so subscribers get a value promptly.
        get()
    }

    // -------------------------------------------------------------------------------------------
    // Strategy implementations
    // -------------------------------------------------------------------------------------------

    private suspend fun getCacheFirst(): T? {
        val memory = memoryCache.getSync(key)
        if (memory != null && !memory.isExpired()) return memory.data

        val disk = diskCache.get(key)
        if (disk != null && !disk.isExpired()) {
            promoteToMemory(key, disk.data)
            return disk.data
        }

        return fetchAndStore()
    }

    private suspend fun getNetworkFirst(): T? {
        return try {
            fetchAndStore()
        } catch (e: Exception) {
            Timber.w(e, "RepositoryCacheCoordinator: network fetch failed, falling back to cache")
            memoryCache.getSync(key)?.data
                ?: diskCache.get(key)?.data
        }
    }

    private fun getMemoryOnly(): T? {
        return memoryCache.getSync(key)?.takeIf { !it.isExpired() }?.data
    }

    private suspend fun getDiskOnly(): T? {
        return diskCache.get(key)?.takeIf { !it.isExpired() }?.data
    }

    private suspend fun getStaleWhileRevalidate(): T? {
        val stale = memoryCache.getSync(key)?.data
            ?: diskCache.get(key)?.data

        // Trigger background revalidation — callers observe the result via observe().
        scope.launch {
            runCatching { fetchAndStore() }
                .onFailure { Timber.w(it, "RepositoryCacheCoordinator: background revalidation failed") }
        }

        return stale
    }

    // -------------------------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------------------------

    private suspend fun fetchAndStore(): T? {
        return try {
            val fresh = networkFetch()
            diskCache.put(key, fresh)
            promoteToMemory(key, fresh)
            fresh
        } catch (e: Exception) {
            Timber.e(e, "RepositoryCacheCoordinator: network fetch failed for key=${key.key}")
            null
        }
    }

    private suspend fun promoteToMemory(key: CacheKey<T>, value: T) {
        memoryCache.put(key, value)
    }
}
