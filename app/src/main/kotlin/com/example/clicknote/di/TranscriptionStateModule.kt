package com.example.clicknote.di

import com.example.clicknote.data.state.TranscriptionServiceStateImpl
import com.example.clicknote.data.state.ActiveServiceStateImpl
import com.example.clicknote.domain.state.TranscriptionServiceState
import com.example.clicknote.domain.state.ActiveServiceState
import dagger.Module
import dagger.Provides
import dagger.Binds
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionStateModule {
    @Binds
    @Singleton
    abstract fun bindTranscriptionServiceState(
        impl: TranscriptionServiceStateImpl
    ): TranscriptionServiceState

    @Binds
    @Singleton
    abstract fun bindActiveServiceState(
        impl: ActiveServiceStateImpl
    ): ActiveServiceState

    companion object {
        @Provides
        @Singleton
        fun provideTranscriptionStateFlow(): MutableStateFlow<TranscriptionServiceState?> = 
            MutableStateFlow(null)
    }
} 