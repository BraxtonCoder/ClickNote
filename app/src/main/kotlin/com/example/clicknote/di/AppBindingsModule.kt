package com.example.clicknote.di

import com.example.clicknote.data.service.*
import com.example.clicknote.data.analytics.MixPanelAnalyticsService
import com.example.clicknote.domain.service.*
import com.example.clicknote.domain.interfaces.NetworkConnectivityManager
import com.example.clicknote.domain.interfaces.NotificationHandler
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.data.network.NetworkConnectivityManagerImpl
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
    abstract fun bindTranscriptionService(
        impl: TranscriptionServiceImpl
    ): TranscriptionService

    @Binds
    @Singleton
    abstract fun bindAudioService(
        impl: AudioServiceImpl
    ): AudioService

    @Binds
    @Singleton
    abstract fun bindStorageService(
        impl: StorageServiceImpl
    ): StorageService

    @Binds
    @Singleton
    abstract fun bindAnalyticsService(
        service: MixPanelAnalyticsService
    ): AnalyticsService

    @Binds
    @Singleton
    abstract fun bindBackupService(
        impl: BackupServiceImpl
    ): BackupService

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
    abstract fun bindUserPreferencesDataStore(
        impl: UserPreferencesDataStoreImpl
    ): UserPreferencesDataStore
} 