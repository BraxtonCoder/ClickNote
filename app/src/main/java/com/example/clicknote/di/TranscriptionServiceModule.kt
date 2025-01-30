package com.example.clicknote.di

import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.service.*
import com.example.clicknote.service.impl.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.Provides
import javax.inject.Singleton
import javax.inject.Provider
import android.content.Context
import okhttp3.OkHttpClient
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.Binds
import dagger.Lazy

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionServiceModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionService(
        impl: CombinedTranscriptionServiceImpl
    ): TranscriptionService

    companion object {
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            return OkHttpClient.Builder().build()
        }
    }
} 