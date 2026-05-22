package com.myapp.core.cache.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.myapp.core.cache.api.Cache
import com.myapp.core.cache.impl.datastore.DataStoreCache
import com.myapp.core.cache.impl.migration.CacheMigration
import com.myapp.core.cache.impl.migration.CacheMigrationManager
import com.myapp.core.cache.impl.memory.LruMemoryCache
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import kotlinx.serialization.json.Json
import javax.inject.Qualifier
import javax.inject.Singleton

// ---------------------------------------------------------------------------
// DataStore delegate — file-level property following AndroidX recommendation
// ---------------------------------------------------------------------------
private val Context.cacheDataStore: DataStore<Preferences>
        by preferencesDataStore(name = "myapp_cache")

// ---------------------------------------------------------------------------
// Qualifier
// ---------------------------------------------------------------------------

/** Qualifies the [Cache] instance backed by DataStore (persistent, disk-based). */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class DiskCache

/** Qualifies the [Json] instance used by the cache layer. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class CacheJson

// ---------------------------------------------------------------------------
// Module
// ---------------------------------------------------------------------------

@Module
@InstallIn(SingletonComponent::class)
abstract class CacheModule {

    @Binds
    @Singleton
    @DiskCache
    abstract fun bindDiskCache(impl: DataStoreCache): Cache

    companion object {

        @Provides
        @Singleton
        fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
            context.cacheDataStore

        @Provides
        @Singleton
        @CacheJson
        fun provideCacheJson(): Json = Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
            coerceInputValues = true
        }

        @Provides
        @Singleton
        fun provideLruMemoryCache(): LruMemoryCache = LruMemoryCache(maxSize = 128)

        @Provides
        @Singleton
        fun provideCacheMigrationManager(
            migrations: Set<@JvmSuppressWildcards CacheMigration>,
        ): CacheMigrationManager = CacheMigrationManager(migrations)
    }

    /** Empty multibinding set — modules can contribute [CacheMigration] implementations here. */
    @Multibinds
    abstract fun cacheMigrations(): Set<CacheMigration>
}
