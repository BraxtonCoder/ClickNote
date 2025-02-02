package com.example.clicknote.di

import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.data.service.impl.OnlineTranscriptionServiceImpl
import com.example.clicknote.data.service.impl.OfflineTranscriptionServiceImpl
import com.example.clicknote.data.service.impl.CombinedTranscriptionServiceImpl
import com.example.clicknote.di.qualifiers.Online
import com.example.clicknote.di.qualifiers.Offline
import com.example.clicknote.di.qualifiers.Combined
import com.example.clicknote.di.qualifiers.Primary
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionServiceModule {
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
        @Primary
        fun providePrimaryTranscriptionService(
            @Combined combinedService: Provider<TranscriptionCapable>
        ): TranscriptionCapable {
            return combinedService.get()
        }
    }
} 