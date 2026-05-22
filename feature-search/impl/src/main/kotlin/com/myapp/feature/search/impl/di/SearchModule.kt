package com.myapp.feature.search.impl.di

import com.myapp.feature.search.api.SearchFeatureEntry
import com.myapp.feature.search.impl.presentation.SearchEntryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SearchModule {

    @Binds
    @Singleton
    abstract fun bindSearchFeatureEntry(impl: SearchEntryImpl): SearchFeatureEntry
}
