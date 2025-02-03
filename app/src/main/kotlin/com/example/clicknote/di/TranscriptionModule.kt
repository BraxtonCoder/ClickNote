package com.example.clicknote.di

import com.example.clicknote.data.repository.TranscriptionRepositoryImpl
import com.example.clicknote.domain.repository.TranscriptionRepository
import com.example.clicknote.domain.usecase.TranscriptionUseCase
import com.example.clicknote.service.TranscriptionManager
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.interfaces.NetworkConnectivityManager
import com.example.clicknote.data.factory.TranscriptionServiceFactoryImpl
import com.example.clicknote.data.provider.TranscriptionServiceProviderImpl
import com.example.clicknote.data.selector.TranscriptionServiceSelectorImpl
import com.example.clicknote.domain.factory.TranscriptionServiceFactory
import com.example.clicknote.domain.provider.TranscriptionServiceProvider
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.service.impl.OnlineTranscriptionServiceImpl
import com.example.clicknote.service.impl.OfflineTranscriptionServiceImpl
import com.example.clicknote.service.impl.CombinedTranscriptionServiceImpl
import com.example.clicknote.domain.provider.TranscriptionEventHandlerProvider
import com.example.clicknote.service.impl.DefaultTranscriptionEventHandlerProvider
import com.example.clicknote.di.qualifiers.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope

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

    @Binds
    @Singleton
    abstract fun bindTranscriptionEventHandlerProvider(
        impl: DefaultTranscriptionEventHandlerProvider
    ): TranscriptionEventHandlerProvider

    @Binds
    @Singleton
    @Online
    abstract fun bindOnlineTranscriptionService(
        impl: OnlineTranscriptionServiceImpl
    ): TranscriptionService

    @Binds
    @Singleton
    @Offline
    abstract fun bindOfflineTranscriptionService(
        impl: OfflineTranscriptionServiceImpl
    ): TranscriptionService

    @Binds
    @Singleton
    @Combined
    abstract fun bindCombinedTranscriptionService(
        impl: CombinedTranscriptionServiceImpl
    ): TranscriptionService

    companion object {
        @Provides
        @Singleton
        fun provideTranscriptionUseCase(
            repository: TranscriptionRepository,
            serviceProvider: TranscriptionServiceProvider,
            serviceSelector: TranscriptionServiceSelector,
            @ApplicationScope scope: CoroutineScope
        ): TranscriptionUseCase {
            return TranscriptionUseCase(repository, serviceProvider, serviceSelector, scope)
        }
    }
} 