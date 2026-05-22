package com.myapp.core.common.crash

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject
import javax.inject.Singleton

interface CrashReporter {
    fun recordException(e: Throwable)
    fun log(message: String)
    fun setKey(key: String, value: String)
}

class NoOpCrashReporter @Inject constructor() : CrashReporter {
    override fun recordException(e: Throwable) = Unit
    override fun log(message: String) = Unit
    override fun setKey(key: String, value: String) = Unit
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CrashReporterModule {

    @Binds
    @Singleton
    abstract fun bindCrashReporter(impl: NoOpCrashReporter): CrashReporter
}
