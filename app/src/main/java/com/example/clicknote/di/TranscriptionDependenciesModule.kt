package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.service.*
import com.example.clicknote.service.impl.*
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.PreferencesRepository
import com.example.clicknote.service.api.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import javax.inject.Provider
import okhttp3.OkHttpClient

@Module
@InstallIn(SingletonComponent::class)
object TranscriptionDependenciesModule {

    @Provides
    @Singleton
    fun provideAudioEnhancer(
        @ApplicationContext context: Context,
        performanceMonitor: Provider<PerformanceMonitor>
    ): AudioEnhancer {
        return AudioEnhancerImpl(context, performanceMonitor.get())
    }

    @Provides
    @Singleton
    fun provideNotificationHandler(
        @ApplicationContext context: Context
    ): NotificationHandler {
        return NotificationHandlerImpl(context)
    }

    @Provides
    @Singleton
    fun providePerformanceMonitor(): PerformanceMonitor {
        return PerformanceMonitorImpl()
    }

    @Provides
    @Singleton
    fun provideOnlineTranscriptionService(
        @ApplicationContext context: Context,
        userPreferences: Provider<UserPreferencesDataStore>,
        performanceMonitor: Provider<PerformanceMonitor>,
        okHttpClient: Provider<OkHttpClient>
    ): OnlineTranscriptionService {
        return OnlineTranscriptionServiceImpl(
            context,
            userPreferences,
            performanceMonitor,
            okHttpClient.get()
        )
    }

    @Provides
    @Singleton
    fun provideOfflineTranscriptionService(
        @ApplicationContext context: Context,
        userPreferences: Provider<UserPreferencesDataStore>,
        performanceMonitor: Provider<PerformanceMonitor>
    ): WhisperOfflineTranscriptionService {
        return WhisperOfflineTranscriptionServiceImpl(
            context,
            userPreferences,
            performanceMonitor
        )
    }

    @Provides
    @Singleton
    fun provideWhisperService(
        @ApplicationContext context: Context,
        openAiApi: Provider<OpenAiApi>,
        userPreferences: Provider<UserPreferencesDataStore>,
        preferencesRepository: Provider<PreferencesRepository>
    ): WhisperService {
        return WhisperServiceImpl(
            context,
            openAiApi.get(),
            userPreferences,
            preferencesRepository
        )
    }

    @Provides
    @Singleton
    fun provideClaudeService(
        @ApplicationContext context: Context,
        userPreferences: Provider<UserPreferencesDataStore>,
        claudeApi: Provider<ClaudeApi>,
        preferencesRepository: Provider<PreferencesRepository>
    ): ClaudeService {
        return ClaudeServiceImpl(
            context,
            userPreferences,
            claudeApi.get(),
            preferencesRepository
        )
    }
} 
