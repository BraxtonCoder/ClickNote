package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.data.service.*
import com.example.clicknote.data.provider.TranscriptionServiceProviderImpl
import com.example.clicknote.domain.service.*
import com.example.clicknote.domain.repository.TranscriptionRepository
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.interfaces.NetworkConnectivityManager
import com.example.clicknote.domain.usecase.TranscriptionUseCase
import com.example.clicknote.di.qualifiers.ApplicationScope
import com.example.clicknote.di.qualifiers.OnlineCapable
import com.example.clicknote.di.qualifiers.OfflineCapable
import com.aallam.openai.client.OpenAI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

@Module
@InstallIn(SingletonComponent::class)
object TranscriptionModule {

    @Provides
    @Singleton
    fun provideTranscriptionServiceProvider(
        @ApplicationContext context: Context,
        transcriptionRepository: TranscriptionRepository,
        transcriptionSelector: TranscriptionServiceSelector,
        connectivityManager: NetworkConnectivityManager,
        preferencesDataStore: UserPreferencesDataStore,
        @OnlineCapable onlineService: TranscriptionCapable,
        @OfflineCapable offlineService: TranscriptionCapable
    ): TranscriptionServiceProvider {
        return TranscriptionServiceProviderImpl(
            transcriptionRepository = transcriptionRepository,
            transcriptionSelector = transcriptionSelector,
            connectivityManager = connectivityManager,
            preferencesDataStore = preferencesDataStore,
            onlineTranscriptionService = onlineService,
            offlineTranscriptionService = offlineService
        )
    }

    @Provides
    @Singleton
    fun provideTranscriptionUseCase(
        transcriptionRepository: TranscriptionRepository,
        serviceProvider: TranscriptionServiceProvider,
        serviceSelector: TranscriptionServiceSelector,
        @ApplicationScope scope: CoroutineScope
    ): TranscriptionUseCase {
        return TranscriptionUseCase(
            repository = transcriptionRepository,
            serviceProvider = serviceProvider,
            serviceSelector = serviceSelector,
            scope = scope
        )
    }

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