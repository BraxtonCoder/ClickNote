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
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.service.impl.OnlineTranscriptionServiceImpl
import com.example.clicknote.service.impl.OfflineTranscriptionServiceImpl
import com.example.clicknote.service.impl.CombinedTranscriptionServiceImpl
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline
import com.example.clicknote.di.qualifiers.Combined
import com.example.clicknote.di.qualifiers.Primary
import com.example.clicknote.di.qualifiers.ApplicationScope
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
    @Online
    abstract fun bindOnlineTranscriptionService(
        impl: OnlineTranscriptionServiceImpl
    ): TranscriptionCapable

    @Binds
    @Singleton
    @Offline
    abstract fun bindOfflineTranscriptionService(
        impl: OfflineTranscriptionServiceImpl
    ): TranscriptionCapable

    @Binds
    @Singleton
    @Combined
    abstract fun bindCombinedTranscriptionService(
        impl: CombinedTranscriptionServiceImpl
    ): TranscriptionCapable

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

        @Provides
        @Singleton
        @Primary
        fun providePrimaryTranscriptionService(
            @Combined combinedService: Provider<TranscriptionCapable>
        ): TranscriptionCapable = combinedService.get()
    }
} 