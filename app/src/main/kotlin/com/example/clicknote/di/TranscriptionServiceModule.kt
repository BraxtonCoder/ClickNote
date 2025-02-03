package com.example.clicknote.di

import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.service.impl.OnlineTranscriptionServiceImpl
import com.example.clicknote.service.impl.OfflineTranscriptionServiceImpl
import com.example.clicknote.service.impl.WhisperOfflineTranscriptionServiceImpl
import com.example.clicknote.service.impl.CombinedTranscriptionService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline

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
} 