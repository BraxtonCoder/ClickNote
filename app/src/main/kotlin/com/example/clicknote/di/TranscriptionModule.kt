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
import com.example.clicknote.domain.factory.TranscriptionServiceFactory
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import com.example.clicknote.domain.transcription.TranscriptionCapable
import com.example.clicknote.di.qualifiers.Primary
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionModule {
    @Binds
    @Singleton
    abstract fun bindTranscriptionRepository(impl: TranscriptionRepositoryImpl): TranscriptionRepository

    @Binds
    @Singleton
    abstract fun bindTranscriptionServiceFactory(impl: TranscriptionServiceFactoryImpl): TranscriptionServiceFactory

    @Binds
    @Singleton
    abstract fun bindTranscriptionServiceProvider(impl: TranscriptionServiceProviderImpl): TranscriptionServiceProvider

    @Binds
    @Singleton
    abstract fun bindTranscriptionServiceSelector(impl: TranscriptionServiceSelectorImpl): TranscriptionServiceSelector

    companion object {
        @Provides
        @Singleton
        fun provideTranscriptionScope(): CoroutineScope {
            return CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }

        @Provides
        @Singleton
        fun provideTranscriptionUseCase(
            repository: TranscriptionRepository,
            serviceProvider: TranscriptionServiceProvider,
            serviceSelector: TranscriptionServiceSelector,
            scope: CoroutineScope
        ): TranscriptionUseCase {
            return TranscriptionUseCase(repository, serviceProvider, serviceSelector, scope)
        }
    }
} 