package com.myapp.feature.feed.impl.di

import com.myapp.feature.feed.api.FeedFeatureEntry
import com.myapp.feature.feed.api.PlayerRepository
import com.myapp.feature.feed.impl.data.repository.FeedRepositoryImpl
import com.myapp.feature.feed.impl.presentation.FeedEntryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FeedModule {

    @Binds
    @Singleton
    abstract fun bindPlayerRepository(impl: FeedRepositoryImpl): PlayerRepository

    @Binds
    @Singleton
    abstract fun bindFeedFeatureEntry(impl: FeedEntryImpl): FeedFeatureEntry
}
