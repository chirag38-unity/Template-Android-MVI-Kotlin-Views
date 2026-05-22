package com.myapp.core.common.id

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface UuidProvider {
    fun generate(): String
}

class RandomUuidProvider @Inject constructor() : UuidProvider {
    override fun generate(): String = UUID.randomUUID().toString()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class UuidProviderModule {

    @Binds
    @Singleton
    abstract fun bindUuidProvider(impl: RandomUuidProvider): UuidProvider
}
