package com.myapp.core.cache.impl.migration

import kotlinx.serialization.json.JsonElement
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Applies a chain of [CacheMigration] steps to upgrade a stored [JsonElement] from
 * [storedVersion] to [targetVersion].
 *
 * Migrations are sorted by [CacheMigration.fromVersion] and applied in sequence.  Any gap in the
 * migration chain causes an [IllegalStateException]; callers should discard the entry in that case.
 */
@Singleton
class CacheMigrationManager @Inject constructor(
    private val migrations: Set<@JvmSuppressWildcards CacheMigration>,
) {

    /**
     * Migrates [data] stored at [storedVersion] to [targetVersion].
     *
     * @return the migrated [JsonElement], or `null` if no migration path exists between the two
     *         versions (callers should treat this as a cache miss and discard the entry).
     */
    suspend fun migrate(
        key: String,
        data: JsonElement,
        storedVersion: Int,
        targetVersion: Int,
    ): JsonElement? {
        if (storedVersion == targetVersion) return data
        if (storedVersion > targetVersion) return null

        val sorted = migrations
            .filter { it.fromVersion >= storedVersion && it.toVersion <= targetVersion }
            .sortedBy { it.fromVersion }

        var current = data
        var currentVersion = storedVersion

        for (migration in sorted) {
            if (migration.fromVersion != currentVersion) return null
            current = migration.migrate(key, current)
            currentVersion = migration.toVersion
        }

        return if (currentVersion == targetVersion) current else null
    }
}
