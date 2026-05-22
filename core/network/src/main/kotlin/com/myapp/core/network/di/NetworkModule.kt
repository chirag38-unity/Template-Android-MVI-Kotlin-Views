package com.myapp.core.network.di

import com.myapp.core.network.client.createHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpClient(): HttpClient {
        return createHttpClient(
            baseUrl = "https://api.example.com/",
            enableLogging = true,
        )
    }
}
