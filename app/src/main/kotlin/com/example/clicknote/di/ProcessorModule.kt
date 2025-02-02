package com.example.clicknote.di

import com.example.clicknote.domain.interfaces.AmplitudeCache
import com.example.clicknote.domain.interfaces.AmplitudeProcessor
import com.example.clicknote.service.impl.AmplitudeCacheImpl
import com.example.clicknote.service.impl.AmplitudeProcessorImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ProcessorModule {
    @Binds
    @Singleton
    abstract fun bindAmplitudeCache(impl: AmplitudeCacheImpl): AmplitudeCache

    @Binds
    @Singleton
    abstract fun bindAmplitudeProcessor(impl: AmplitudeProcessorImpl): AmplitudeProcessor
}