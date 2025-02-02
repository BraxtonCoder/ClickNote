package com.example.clicknote.di

import com.example.clicknote.data.service.*
import com.example.clicknote.domain.service.*
import com.example.clicknote.domain.interfaces.*
import com.example.clicknote.domain.service.WhisperTranscriptionService
import com.example.clicknote.domain.service.WhisperOfflineTranscriptionService
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
    abstract fun bindNotificationService(
        impl: NotificationServiceImpl
    ): NotificationService

    @Binds
    @Singleton
    abstract fun bindAuthService(
        impl: AuthServiceImpl
    ): AuthService

    @Binds
    @Singleton
    abstract fun bindAnalyticsService(
        impl: AnalyticsServiceImpl
    ): AnalyticsService

    @Binds
    @Singleton
    abstract fun bindBackupService(
        impl: BackupServiceImpl
    ): BackupService

    @Binds
    @Singleton
    abstract fun bindBillingService(
        impl: BillingServiceImpl
    ): BillingService

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
    abstract fun bindUserPreferencesDataStore(
        impl: UserPreferencesDataStoreImpl
    ): UserPreferencesDataStore

    /*
    @Binds
    @Singleton
    abstract fun bindTranscriptionEventHandler(
        impl: TranscriptionEventHandlerImpl
    ): TranscriptionEventHandler
    */
} 