package com.myapp.core.cache.impl.migration

import kotlinx.serialization.json.JsonElement

/**
 * Represents a single migration step that transforms the raw [JsonElement] stored for a cache
 * entry from one schema version to another.
 *
 * Implement this interface and register instances with [CacheMigrationManager] to automatically
 * upgrade persisted entries when their stored version is lower than the current [CacheKey.version].
 */
interface CacheMigration {
    /** The schema version this migration reads from. */
    val fromVersion: Int

    /** The schema version this migration produces. */
    val toVersion: Int

    /**
     * Transforms [data] stored under [key] from [fromVersion] to [toVersion] format.
     *
     * @param key the string identifier of the cache entry being migrated
     * @param data the raw [JsonElement] at version [fromVersion]
     * @return the transformed [JsonElement] compatible with [toVersion]
     */
    suspend fun migrate(key: String, data: JsonElement): JsonElement
}
