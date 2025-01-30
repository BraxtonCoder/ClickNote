package com.example.clicknote.di

import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.PowerManager
import android.os.Vibrator
import androidx.core.app.NotificationManagerCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import com.example.clicknote.service.*
import com.example.clicknote.service.impl.*
import com.example.clicknote.service.feedback.FeedbackService
import com.example.clicknote.domain.service.*
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.data.preferences.UserPreferencesDataStoreImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {

    @Provides
    @Singleton
    fun provideVibrationHandler(impl: VibrationHandlerImpl): VibrationHandler {
        return impl
    }

    @Provides
    @Singleton
    fun provideVolumeButtonHandler(impl: VolumeButtonHandlerImpl): VolumeButtonHandler {
        return impl
    }

    @Provides
    @Singleton
    fun provideStorageService(impl: StorageServiceImpl): StorageService {
        return impl
    }

    @Provides
    @Singleton
    fun provideSpeakerProfileService(impl: SpeakerProfileServiceImpl): SpeakerProfileService {
        return impl
    }

    @Provides
    @Singleton
    fun provideClipboardManager(impl: ClipboardManagerImpl): ClipboardManager {
        return impl
    }

    @Provides
    @Singleton
    fun provideClipboardService(impl: ClipboardServiceImpl): ClipboardService {
        return impl
    }

    @Provides
    @Singleton
    fun provideFeedbackService(impl: FeedbackServiceImpl): FeedbackService {
        return impl
    }

    @Provides
    @Singleton
    fun provideAudioManager(@ApplicationContext context: Context): AudioManager {
        return context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @Provides
    @Singleton
    fun provideVibrator(@ApplicationContext context: Context): Vibrator {
        return context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    @Provides
    @Singleton
    fun provideNotificationManager(@ApplicationContext context: Context): NotificationManager {
        return context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    @Provides
    @Singleton
    fun provideNotificationManagerCompat(@ApplicationContext context: Context): NotificationManagerCompat {
        return NotificationManagerCompat.from(context)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            produceFile = { context.preferencesDataStoreFile("user_preferences") }
        )
    }

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(impl: UserPreferencesDataStoreImpl): UserPreferencesDataStore {
        return impl
    }

    @Provides
    @Singleton
    fun provideSubscriptionStateObserver(
        @ApplicationContext context: Context,
        authService: AuthService,
        subscriptionStateManager: SubscriptionStateManager
    ): SubscriptionStateObserver {
        return SubscriptionStateObserver(context, authService, subscriptionStateManager)
    }

    @Provides
    @Singleton
    fun provideCloudSyncService(impl: CloudSyncServiceImpl): CloudSyncService {
        return impl
    }

    @Provides
    @Singleton
    fun provideFirestoreService(impl: FirestoreServiceImpl): FirestoreService {
        return impl
    }

    @Provides
    @Singleton
    fun provideRecordingService(impl: RecordingServiceImpl): RecordingService {
        return impl
    }

    @Provides
    @Singleton
    fun provideSummaryService(impl: SummaryServiceImpl): SummaryService {
        return impl
    }
} 