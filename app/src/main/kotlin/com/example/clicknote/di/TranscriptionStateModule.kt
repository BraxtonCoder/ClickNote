package com.example.clicknote.di

import com.example.clicknote.data.state.TranscriptionServiceStateImpl
import com.example.clicknote.domain.state.TranscriptionServiceState
import com.example.clicknote.service.TranscriptionStateManager
import com.example.clicknote.service.impl.DefaultTranscriptionStateManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(SingletonComponent::class)
object TranscriptionStateModule {
    @Provides
    @Singleton
    fun provideTranscriptionServiceState(): TranscriptionServiceState = 
        TranscriptionServiceStateImpl()

    @Provides
    @Singleton
    fun provideTranscriptionStateFlow(): MutableStateFlow<TranscriptionServiceState?> = 
        MutableStateFlow(null)

    @Provides
    @Singleton
    fun provideTranscriptionStateManager(
        serviceState: TranscriptionServiceState
    ): TranscriptionStateManager = 
        DefaultTranscriptionStateManager(serviceState)
} 