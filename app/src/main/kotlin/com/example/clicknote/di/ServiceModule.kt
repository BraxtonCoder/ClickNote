package com.example.clicknote.di

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import com.example.clicknote.data.service.*
import com.example.clicknote.domain.service.*
import com.example.clicknote.data.strategy.ServiceStrategyImpl
import com.example.clicknote.domain.strategy.ServiceStrategy
import com.example.clicknote.data.handler.ServiceEventHandlerImpl
import com.example.clicknote.domain.event.ServiceEventHandler
import com.example.clicknote.service.TranscriptionManager
import com.example.clicknote.service.TranscriptionManagerImpl
import com.example.clicknote.service.recording.IRecordingService
import com.example.clicknote.service.recording.RecordingServiceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ServicePowerManager

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ServiceNotificationManager

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {

    @Binds
    @Singleton
    abstract fun bindServiceStrategy(
        impl: ServiceStrategyImpl
    ): ServiceStrategy

    @Binds
    @Singleton
    abstract fun bindServiceEventHandler(
        impl: ServiceEventHandlerImpl
    ): ServiceEventHandler

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
    abstract fun bindBillingService(
        impl: BillingServiceImpl
    ): BillingService

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
    abstract fun bindTranscriptionManager(
        impl: TranscriptionManagerImpl
    ): TranscriptionManager

    @Binds
    abstract fun bindRecordingService(
        impl: RecordingServiceImpl
    ): IRecordingService

    companion object {
        @Provides
        @ServicePowerManager
        @Singleton
        fun providePowerManager(
            @ApplicationContext context: Context
        ): PowerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

        @Provides
        @ServiceNotificationManager
        @Singleton
        fun provideNotificationManager(
            @ApplicationContext context: Context
        ): NotificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        @Provides
        @Singleton
        fun provideAudioManager(
            @ApplicationContext context: Context
        ): AudioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }
}