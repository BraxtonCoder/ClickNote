package com.example.clicknote.di

import com.example.clicknote.data.state.ServiceStateManagerImpl
import com.example.clicknote.domain.state.ServiceState
import com.example.clicknote.domain.state.ServiceStateManager
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class TranscriptionStateModule {

    @Binds
    @Singleton
    abstract fun bindServiceStateManager(
        impl: ServiceStateManagerImpl
    ): ServiceStateManager

    companion object {
        @Provides
        @Singleton
        fun provideServiceStateFlow(): MutableStateFlow<ServiceState?> =
            MutableStateFlow(null)
    }
} 