package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.service.analytics.AnalyticsService
import com.example.clicknote.service.analytics.AnalyticsServiceImpl
import com.example.clicknote.service.auth.AuthService
import com.example.clicknote.service.auth.AuthServiceImpl
import com.example.clicknote.service.backup.BackupService
import com.example.clicknote.service.backup.BackupServiceImpl
import com.example.clicknote.service.billing.BillingService
import com.example.clicknote.service.billing.BillingServiceImpl
import com.example.clicknote.service.notification.NotificationService
import com.example.clicknote.service.notification.NotificationServiceImpl
import com.example.clicknote.service.storage.StorageService
import com.example.clicknote.service.storage.StorageServiceImpl
import com.example.clicknote.service.transcription.TranscriptionService
import com.example.clicknote.service.transcription.TranscriptionServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    
    @Binds
    @Singleton
    abstract fun bindAnalyticsService(impl: AnalyticsServiceImpl): AnalyticsService

    @Binds
    @Singleton
    abstract fun bindAuthService(impl: AuthServiceImpl): AuthService

    @Binds
    @Singleton
    abstract fun bindBackupService(impl: BackupServiceImpl): BackupService

    @Binds
    @Singleton
    abstract fun bindBillingService(impl: BillingServiceImpl): BillingService

    @Binds
    @Singleton
    abstract fun bindNotificationService(impl: NotificationServiceImpl): NotificationService

    @Binds
    @Singleton
    abstract fun bindStorageService(impl: StorageServiceImpl): StorageService

    @Binds
    @Singleton
    abstract fun bindTranscriptionService(impl: TranscriptionServiceImpl): TranscriptionService
} 