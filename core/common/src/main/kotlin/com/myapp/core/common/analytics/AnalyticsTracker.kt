package com.myapp.core.common.analytics

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface AnalyticsTracker {
    fun trackEvent(name: String, params: Map<String, Any> = emptyMap())
    fun trackScreen(name: String)
    fun setUserProperty(key: String, value: String)
}

class NoOpAnalyticsTracker @Inject constructor() : AnalyticsTracker {
    override fun trackEvent(name: String, params: Map<String, Any>) = Unit
    override fun trackScreen(name: String) = Unit
    override fun setUserProperty(key: String, value: String) = Unit
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {

    @Binds
    @Singleton
    abstract fun bindAnalyticsTracker(impl: NoOpAnalyticsTracker): AnalyticsTracker
}
