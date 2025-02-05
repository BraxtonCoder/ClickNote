package com.example.clicknote.di

import com.example.clicknote.data.service.*
import com.example.clicknote.domain.service.*
import com.example.clicknote.domain.selector.TranscriptionServiceSelector
import com.example.clicknote.data.selector.TranscriptionServiceSelectorImpl
import com.example.clicknote.data.event.TranscriptionEventHandlerImpl
import com.example.clicknote.domain.event.TranscriptionEventHandler
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionBindingsModule {

    @Binds
    @Singleton
    abstract fun bindTranscriptionServiceSelector(
        impl: TranscriptionServiceSelectorImpl
    ): TranscriptionServiceSelector

    @Binds
    @Singleton
    abstract fun bindOnlineTranscriptionService(
        impl: OpenAITranscriptionService
    ): TranscriptionCapable

    @Binds
    @Singleton
    abstract fun bindOfflineTranscriptionService(
        impl: WhisperOfflineTranscriptionService
    ): TranscriptionCapable

    @Binds
    @Singleton
    abstract fun bindTranscriptionEventHandler(
        impl: TranscriptionEventHandlerImpl
    ): TranscriptionEventHandler

    @Binds
    @Singleton
    abstract fun bindWhisperTranscriptionService(
        impl: WhisperTranscriptionServiceImpl
    ): WhisperTranscriptionService

    @Binds
    @Singleton
    abstract fun bindWhisperOfflineTranscriptionService(
        impl: WhisperOfflineTranscriptionServiceImpl
    ): WhisperOfflineTranscriptionService
} 