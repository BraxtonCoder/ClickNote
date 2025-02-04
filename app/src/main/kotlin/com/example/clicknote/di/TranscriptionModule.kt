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
import com.example.clicknote.domain.provider.TranscriptionEventHandlerProvider
import com.example.clicknote.service.impl.DefaultTranscriptionEventHandlerProvider
import com.example.clicknote.di.qualifiers.*
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import dagger.Lazy

@Module
@InstallIn(SingletonComponent::class)
object TranscriptionModule {
    @Provides
    @Singleton
    fun provideTranscriptionServiceSelector(): TranscriptionServiceSelector {
        return TranscriptionServiceSelectorImpl()
    }

    @Provides
    @Singleton
    fun provideTranscriptionServiceProvider(
        selector: Lazy<TranscriptionServiceSelector>
    ): TranscriptionServiceProvider {
        return TranscriptionServiceProviderImpl(selector)
    }

    @Provides
    @Singleton
    fun provideTranscriptionServiceFactory(
        provider: Lazy<TranscriptionServiceProvider>
    ): TranscriptionServiceFactory {
        return TranscriptionServiceFactoryImpl(provider)
    }

    @Provides
    @Singleton
    fun provideTranscriptionRepository(
        factory: Lazy<TranscriptionServiceFactory>,
        provider: Lazy<TranscriptionServiceProvider>,
        selector: Lazy<TranscriptionServiceSelector>
    ): TranscriptionRepository {
        return TranscriptionRepositoryImpl(factory, provider, selector)
    }

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
            repository: Lazy<TranscriptionRepository>,
            serviceProvider: Lazy<TranscriptionServiceProvider>,
            serviceSelector: Lazy<TranscriptionServiceSelector>,
            @ApplicationScope scope: CoroutineScope
        ): TranscriptionUseCase {
            return TranscriptionUseCase(repository, serviceProvider, serviceSelector, scope)
        }
    }
} 