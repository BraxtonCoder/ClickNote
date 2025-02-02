package com.example.clicknote.di

import com.example.clicknote.data.provider.TranscriptionServiceProviderImpl
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProviderModule {
    @Binds
    @Singleton
    abstract fun bindTranscriptionServiceProvider(
        impl: TranscriptionServiceProviderImpl
    ): TranscriptionServiceProvider
} 