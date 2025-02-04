package com.example.clicknote.di

import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.service.impl.OnlineTranscriptionServiceImpl
import com.example.clicknote.service.impl.OfflineTranscriptionServiceImpl
import com.example.clicknote.service.impl.WhisperOfflineTranscriptionServiceImpl
import com.example.clicknote.service.impl.CombinedTranscriptionService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline
import com.example.clicknote.di.qualifiers.Combined

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionServiceModule {

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
    abstract fun bindWhisperOfflineService(
        impl: WhisperOfflineTranscriptionServiceImpl
    ): WhisperOfflineTranscriptionService

    @Binds
    @Singleton
    abstract fun bindCombinedTranscriptionService(
        impl: CombinedTranscriptionService
    ): TranscriptionService

    companion object {
        @Provides
        @Singleton
        @Combined
        fun provideCombinedTranscriptionService(
            @Online onlineService: Provider<TranscriptionService>,
            @Offline offlineService: Provider<TranscriptionService>
        ): TranscriptionService {
            return CombinedTranscriptionService(onlineService.get(), offlineService.get())
        }
    }
} 