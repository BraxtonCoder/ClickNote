package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.data.service.*
import com.example.clicknote.domain.service.*
import com.example.clicknote.domain.interfaces.NetworkConnectivityManager
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.di.qualifiers.OnlineCapable
import com.example.clicknote.di.qualifiers.OfflineCapable
import com.aallam.openai.client.OpenAI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object TranscriptionServiceModule {

    @Provides
    @Singleton
    @OnlineCapable
    fun provideOnlineTranscriptionService(
        @ApplicationContext context: Context,
        connectivityManager: NetworkConnectivityManager,
        preferencesDataStore: UserPreferencesDataStore,
        openAI: OpenAI
    ): TranscriptionCapable {
        return WhisperTranscriptionServiceImpl(
            context = context,
            connectivityManager = connectivityManager,
            preferencesDataStore = preferencesDataStore,
            openAI = openAI
        )
    }

    @Provides
    @Singleton
    @OfflineCapable
    fun provideOfflineTranscriptionService(
        @ApplicationContext context: Context,
        connectivityManager: NetworkConnectivityManager,
        preferencesDataStore: UserPreferencesDataStore
    ): TranscriptionCapable {
        return WhisperOfflineTranscriptionServiceImpl(
            context = context,
            connectivityManager = connectivityManager,
            preferencesDataStore = preferencesDataStore
        )
    }
} 