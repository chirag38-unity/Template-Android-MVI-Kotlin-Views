package com.myapp.core.cache.api

/**
 * Defines the strategy used when resolving a cached value.
 *
 * - [CACHE_FIRST]            Return memory → disk hit, only fetch from network on miss or expiry.
 * - [NETWORK_FIRST]          Always fetch from network; update caches on success; fall back to
 *                             cache on network failure.
 * - [MEMORY_ONLY]            Only read/write the in-memory LRU cache; never touch disk or network.
 * - [DISK_ONLY]              Only read/write the persistent disk cache; skip memory and network.
 * - [STALE_WHILE_REVALIDATE] Return the cached value immediately (even if stale), then trigger a
 *                             background network refresh and emit the fresh value when available.
 */
enum class CachePolicy {
    CACHE_FIRST,
    NETWORK_FIRST,
    MEMORY_ONLY,
    DISK_ONLY,
    STALE_WHILE_REVALIDATE,
}
