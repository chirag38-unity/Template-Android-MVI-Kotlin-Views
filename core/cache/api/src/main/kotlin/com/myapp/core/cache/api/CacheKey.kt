package com.myapp.core.cache.api

import kotlinx.serialization.KSerializer
import kotlin.time.Duration

/**
 * A strongly-typed key that identifies a cache entry.
 *
 * @param T the type of the cached value
 * @property key unique string identifier for this cache entry
 * @property serializer Kotlinx Serialization serializer for [T]
 * @property ttl optional time-to-live; null means the entry never expires
 * @property version schema version used for cache invalidation on schema changes
 */
data class CacheKey<T : Any>(
    val key: String,
    val serializer: KSerializer<T>,
    val ttl: Duration? = null,
    val version: Int = 1,
)
