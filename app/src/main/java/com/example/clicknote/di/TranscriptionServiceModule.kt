package com.example.clicknote.di

import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.service.impl.CombinedTranscriptionServiceImpl
import com.example.clicknote.service.impl.OfflineTranscriptionServiceImpl
import com.example.clicknote.service.impl.OnlineTranscriptionServiceImpl
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionServiceModule {
    @Binds
    @Singleton
    abstract fun bindTranscriptionService(
        impl: CombinedTranscriptionServiceImpl
    ): TranscriptionCapable

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
}