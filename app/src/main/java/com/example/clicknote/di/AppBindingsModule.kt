package com.example.clicknote.di

import com.example.clicknote.domain.interfaces.*
import com.example.clicknote.service.impl.*
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppBindingsModule {
    @Binds
    @Singleton
    abstract fun bindTranscriptionEventHandler(
        impl: TranscriptionEventHandlerImpl
    ): TranscriptionEventHandler

    @Binds
    @Singleton
    abstract fun bindTranscriptionStateManager(
        impl: TranscriptionStateManagerImpl
    ): TranscriptionStateManager

    @Binds
    @Singleton
    abstract fun bindNotificationHandler(
        impl: NotificationHandlerImpl
    ): NotificationHandler

    @Binds
    @Singleton
    abstract fun bindNetworkConnectivityManager(
        impl: NetworkConnectivityManagerImpl
    ): NetworkConnectivityManager
} 