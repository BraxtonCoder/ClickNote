package com.example.clicknote.di

import com.example.clicknote.data.factory.TranscriptionServiceFactoryImpl
import com.example.clicknote.domain.factory.TranscriptionServiceFactory
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class FactoryModule {
    @Binds
    @Singleton
    abstract fun bindTranscriptionServiceFactory(
        impl: TranscriptionServiceFactoryImpl
    ): TranscriptionServiceFactory
} 