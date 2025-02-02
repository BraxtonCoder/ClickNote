package com.example.clicknote.di

import com.example.clicknote.data.service.PerformanceMonitorImpl
import com.example.clicknote.domain.service.PerformanceMonitor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MonitorModule {
    @Binds
    @Singleton
    abstract fun bindPerformanceMonitor(impl: PerformanceMonitorImpl): PerformanceMonitor
} 