package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.service.AmplitudeProcessor
import com.example.clicknote.service.PerformanceMonitor
import com.example.clicknote.service.impl.AmplitudeProcessorImpl
import com.example.clicknote.service.impl.PerformanceMonitorImpl
import com.example.clicknote.domain.interfaces.AmplitudeCache
import com.example.clicknote.service.impl.AmplitudeCacheImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ProcessorModule {
    @Provides
    @Singleton
    fun providePerformanceMonitor(@ApplicationContext context: Context): PerformanceMonitor =
        PerformanceMonitorImpl(context)

    @Provides
    @Singleton
    fun provideAmplitudeCache(@ApplicationContext context: Context): AmplitudeCache =
        AmplitudeCacheImpl(context)

    @Provides
    @Singleton
    fun provideAmplitudeProcessor(
        performanceMonitor: PerformanceMonitor,
        amplitudeCache: AmplitudeCache
    ): AmplitudeProcessor = AmplitudeProcessorImpl(performanceMonitor, amplitudeCache)
} 