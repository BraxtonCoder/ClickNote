package com.example.clicknote.di

import com.example.clicknote.data.repository.TranscriptionRepositoryImpl
import com.example.clicknote.domain.repository.TranscriptionRepository
import com.example.clicknote.domain.usecase.TranscriptionUseCase
import com.example.clicknote.service.TranscriptionManager
import com.example.clicknote.service.TranscriptionEventHandler
import com.example.clicknote.service.TranscriptionStateManager
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.interfaces.NetworkConnectivityManager
import com.example.clicknote.data.factory.TranscriptionServiceFactoryImpl
import com.example.clicknote.data.provider.TranscriptionServiceProviderImpl
import com.example.clicknote.data.selector.TranscriptionServiceSelectorImpl
import com.example.clicknote.data.state.TranscriptionServiceStateImpl
import com.example.clicknote.domain.factory.TranscriptionServiceFactory
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import com.example.clicknote.domain.service.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module(includes = [TranscriptionModule.Bindings::class])
@InstallIn(SingletonComponent::class)
object TranscriptionModule {

    @Module
    @InstallIn(SingletonComponent::class)
    interface Bindings {
        @Binds
        @Singleton
        fun bindTranscriptionRepository(impl: TranscriptionRepositoryImpl): TranscriptionRepository

        @Binds
        @Singleton
        fun bindTranscriptionServiceFactory(impl: TranscriptionServiceFactoryImpl): TranscriptionServiceFactory

        @Binds
        @Singleton
        fun bindTranscriptionServiceProvider(impl: TranscriptionServiceProviderImpl): TranscriptionServiceProvider

        @Binds
        @Singleton
        fun bindTranscriptionServiceSelector(impl: TranscriptionServiceSelectorImpl): TranscriptionServiceSelector
    }

    @Provides
    @Singleton
    fun provideTranscriptionUseCase(
        repository: Provider<TranscriptionRepository>,
        eventHandler: Provider<TranscriptionEventHandler>,
        stateManager: Provider<TranscriptionStateManager>
    ): TranscriptionUseCase {
        return TranscriptionUseCase(repository, eventHandler, stateManager)
    }

    @Provides
    @Singleton
    fun provideTranscriptionManager(
        useCase: Provider<TranscriptionUseCase>,
        eventHandler: Provider<TranscriptionEventHandler>,
        stateManager: Provider<TranscriptionStateManager>,
        userPreferences: Provider<UserPreferencesDataStore>,
        connectivityManager: Provider<NetworkConnectivityManager>
    ): TranscriptionManager {
        return TranscriptionManager(
            useCase,
            eventHandler,
            stateManager,
            userPreferences,
            connectivityManager
        )
    }
} 