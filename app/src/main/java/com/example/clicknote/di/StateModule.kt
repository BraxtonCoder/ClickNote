package com.example.clicknote.di

import com.example.clicknote.domain.model.SubscriptionStateManager
import com.example.clicknote.service.SubscriptionStateManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StateModule {

    @Binds
    @Singleton
    abstract fun bindSubscriptionStateManager(
        impl: SubscriptionStateManagerImpl
    ): SubscriptionStateManager
} 