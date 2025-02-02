package com.example.clicknote.di

import com.example.clicknote.domain.interfaces.*
import com.example.clicknote.domain.service.WhisperTranscriptionService
import com.example.clicknote.domain.service.WhisperOfflineTranscriptionService
import com.example.clicknote.domain.service.PerformanceMonitor
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.impl.*
import com.example.clicknote.data.preferences.UserPreferencesDataStoreImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    @Singleton
    abstract fun bindTranscriptionEventHandler(
        impl: TranscriptionEventHandlerImpl
    ): TranscriptionEventHandler

    @Binds
    @Singleton
    abstract fun bindTranscriptionStateManager(
        impl: TranscriptionStateManagerImpl
    ): TranscriptionStateManager

    @Binds
    @Singleton
    abstract fun bindNotificationHandler(
        impl: NotificationHandlerImpl
    ): NotificationHandler

    @Binds
    @Singleton
    abstract fun bindNetworkConnectivityManager(
        impl: NetworkConnectivityManagerImpl
    ): NetworkConnectivityManager

    @Binds
    @Singleton
    abstract fun bindWhisperTranscriptionService(
        impl: WhisperTranscriptionServiceImpl
    ): WhisperTranscriptionService

    @Binds
    @Singleton
    abstract fun bindWhisperOfflineTranscriptionService(
        impl: WhisperOfflineTranscriptionServiceImpl
    ): WhisperOfflineTranscriptionService

    @Binds
    @Singleton
    abstract fun bindPerformanceMonitor(
        impl: PerformanceMonitorImpl
    ): PerformanceMonitor

    @Binds
    @Singleton
    abstract fun bindUserPreferencesDataStore(
        impl: UserPreferencesDataStoreImpl
    ): UserPreferencesDataStore
} 