package com.example.clicknote.di

import com.example.clicknote.domain.state.ServiceStateManager
import com.example.clicknote.data.state.ServiceStateManagerImpl
import com.example.clicknote.data.state.TranscriptionServiceStateImpl
import com.example.clicknote.domain.state.TranscriptionServiceState
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(SingletonComponent::class)
interface StateModule {
    @Binds
    @Singleton
    fun bindServiceStateManager(
        impl: ServiceStateManagerImpl
    ): ServiceStateManager

    @Binds
    @Singleton
    fun bindTranscriptionServiceState(
        impl: TranscriptionServiceStateImpl
    ): TranscriptionServiceState
}

@Module
@InstallIn(SingletonComponent::class)
object StateProviderModule {
    @Provides
    @Singleton
    fun provideTranscriptionStateFlow(): MutableStateFlow<TranscriptionServiceState?> = 
        MutableStateFlow(null)
} 