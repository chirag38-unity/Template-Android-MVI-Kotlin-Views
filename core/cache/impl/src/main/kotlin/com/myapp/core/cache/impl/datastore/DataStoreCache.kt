package com.myapp.core.cache.impl.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.myapp.core.cache.api.Cache
import com.myapp.core.cache.api.CacheKey
import com.myapp.core.cache.api.CachedValue
import com.myapp.core.cache.impl.di.CacheJson
import com.myapp.core.cache.impl.migration.CacheMigrationManager
import com.myapp.core.cache.impl.serializers.CachedValueDto
import com.myapp.core.common.dispatchers.AppDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.json.Json
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A persistent [Cache] backed by [Preferences DataStore][DataStore].
 *
 * Each entry is serialised as a JSON string and stored under its [CacheKey.key] as a
 * [Preferences] string key.  The raw JSON payload is a [CachedValueDto] whose `data` field
 * holds the type-erased [JsonElement]; the concrete type is restored using the
 * [KSerializer][kotlinx.serialization.KSerializer] embedded in [CacheKey].
 *
 * Guarantees:
 * - No unsafe casts — type safety is delegated entirely to [Json] and the key's serialiser.
 * - Corruption is handled safely; a corrupt entry is treated as a cache miss and removed.
 * - Expired entries are removed lazily on the first access after expiry.
 * - All IO runs on [AppDispatchers.io].
 * - Thread-safe: DataStore guarantees sequential writes.
 */
@Singleton
class DataStoreCache @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @CacheJson private val json: Json,
    private val migrationManager: CacheMigrationManager,
    private val dispatchers: AppDispatchers,
) : Cache {

    @OptIn(InternalSerializationApi::class)
    override suspend fun <T : Any> put(key: CacheKey<T>, value: T) {
        withContext(dispatchers.io) {
            val now = System.currentTimeMillis()
            val expiresAt = key.ttl?.let { now + it.inWholeMilliseconds }
            val dto = CachedValueDto(
                data = json.encodeToJsonElement(key.serializer, value),
                createdAt = now,
                expiresAt = expiresAt,
                version = key.version,
            )
            val encoded = json.encodeToString(CachedValueDto.serializer(), dto)
            dataStore.edit { prefs ->
                prefs[stringPreferencesKey(key.key)] = encoded
            }
        }
    }

    override suspend fun <T : Any> get(key: CacheKey<T>): CachedValue<T>? {
        return withContext(dispatchers.io) {
            val prefs = dataStore.data.first()
            val encoded = prefs[stringPreferencesKey(key.key)] ?: return@withContext null
            val cached = decodeEntry(key, encoded) ?: return@withContext null
            if (cached.isExpired()) {
                remove(key)
                return@withContext null
            }
            cached
        }
    }

    override suspend fun <T : Any> remove(key: CacheKey<T>) {
        withContext(dispatchers.io) {
            dataStore.edit { prefs ->
                prefs.remove(stringPreferencesKey(key.key))
            }
        }
    }

    override suspend fun clear() {
        withContext(dispatchers.io) {
            dataStore.edit { it.clear() }
        }
    }

    override fun <T : Any> observe(key: CacheKey<T>): Flow<CachedValue<T>?> {
        return dataStore.data
            .map { prefs ->
                val encoded = prefs[stringPreferencesKey(key.key)]
                    ?: return@map null
                // Decode without side-effects (no removal) to avoid triggering a re-emission loop.
                decodeEntryReadOnly(key, encoded)
            }
            .catch { e ->
                Timber.e(e, "DataStoreCache observe error for key=${key.key}")
                emit(null)
            }
            .flowOn(dispatchers.io)
    }

    // -------------------------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------------------------

    /**
     * Decodes a stored entry and applies migrations if necessary.
     * On corrupt data the entry is removed from the store (safe to call from `get`).
     */
    private suspend fun <T : Any> decodeEntry(key: CacheKey<T>, encoded: String): CachedValue<T>? {
        return runCatching {
            decodeDto(key, encoded)
        }.getOrElse { e ->
            Timber.e(e, "DataStoreCache decode error for key=${key.key}; discarding corrupt entry")
            removeSilently(key.key)
            null
        }
    }

    /**
     * Decodes a stored entry without any DataStore side-effects.
     * Used inside [observe] to prevent re-emission cycles.
     */
    @OptIn(InternalSerializationApi::class)
    private fun <T : Any> decodeEntryReadOnly(key: CacheKey<T>, encoded: String): CachedValue<T>? {
        return runCatching {
            // Note: cannot call the suspend decodeDto here; run the JSON decode synchronously.
            val dto = json.decodeFromString(CachedValueDto.serializer(), encoded)
            if (dto.isExpired()) return null
            // Skip migration in the read-only path; a stale-version entry is treated as a miss
            // until the next suspending get() call applies the migration and updates the store.
            if (dto.version != key.version) return null
            val data = json.decodeFromJsonElement(key.serializer, dto.data)
            CachedValue(data = data, createdAt = dto.createdAt, expiresAt = dto.expiresAt, version = key.version)
        }.getOrElse { e ->
            Timber.e(e, "DataStoreCache observe decode error for key=${key.key}")
            null
        }
    }

    @OptIn(InternalSerializationApi::class)
    private suspend fun <T : Any> decodeDto(key: CacheKey<T>, encoded: String): CachedValue<T>? {
        val dto = json.decodeFromString(CachedValueDto.serializer(), encoded)

        val migratedData = if (dto.version != key.version) {
            migrationManager.migrate(
                key = key.key,
                data = dto.data,
                storedVersion = dto.version,
                targetVersion = key.version,
            ) ?: run {
                Timber.w("No migration path for key=${key.key} v${dto.version}→v${key.version}; discarding")
                return null
            }
        } else {
            dto.data
        }

        val data = json.decodeFromJsonElement(key.serializer, migratedData)
        return CachedValue(
            data = data,
            createdAt = dto.createdAt,
            expiresAt = dto.expiresAt,
            version = key.version,
        )
    }

    private suspend fun removeSilently(keyStr: String) {
        runCatching {
            dataStore.edit { prefs -> prefs.remove(stringPreferencesKey(keyStr)) }
        }
    }
}
