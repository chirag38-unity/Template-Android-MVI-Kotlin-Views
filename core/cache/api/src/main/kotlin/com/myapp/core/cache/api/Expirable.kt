package com.myapp.core.cache.api

/**
 * An entity that has an optional expiration timestamp.
 */
interface Expirable {
    val expiresAt: Long?

    /** Returns true if this entity has passed its expiry time. */
    fun isExpired(): Boolean = expiresAt != null && System.currentTimeMillis() > expiresAt!!
}
