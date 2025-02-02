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
abstract class StateModule {
    @Binds
    @Singleton
    abstract fun bindTranscriptionServiceState(
        impl: TranscriptionServiceStateImpl
    ): TranscriptionServiceState

    companion object {
        @Provides
        @Singleton
        fun provideStateFlows() = MutableStateFlow<TranscriptionServiceState?>(null)
    }
} 