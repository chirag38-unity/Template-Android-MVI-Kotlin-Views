package com.myapp.feature.player.details.impl.di

import com.myapp.feature.player.details.api.PlayerDetailsFeatureEntry
import com.myapp.feature.player.details.impl.presentation.PlayerDetailsEntryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class PlayerDetailsModule {

    @Binds
    @Singleton
    abstract fun bindPlayerDetailsEntry(impl: PlayerDetailsEntryImpl): PlayerDetailsFeatureEntry
}
