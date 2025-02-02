package com.example.clicknote.di

import com.example.clicknote.domain.DefaultDispatcherProvider
import com.example.clicknote.domain.DispatcherProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DispatcherModule {
    @Provides
    @Singleton
    fun provideDispatcherProvider(dispatcherProvider: DefaultDispatcherProvider): DispatcherProvider {
        return dispatcherProvider
    }
} 