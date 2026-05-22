package com.myapp.core.cache.api

/**
 * A wrapper that pairs a cached value with its persistence metadata.
 *
 * @param T the type of the cached data
 * @property data the actual cached value
 * @property createdAt epoch-millis when this entry was stored
 * @property expiresAt epoch-millis after which this entry is considered stale; null = no expiry
 * @property version schema version at the time of storage, used for migration checks
 */
data class CachedValue<T : Any>(
    val data: T,
    val createdAt: Long = System.currentTimeMillis(),
    override val expiresAt: Long? = null,
    val version: Int = 1,
) : Expirable
