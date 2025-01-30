package com.example.clicknote.di

import com.example.clicknote.domain.model.SubscriptionStateManager
import com.example.clicknote.domain.service.SubscriptionService
import com.example.clicknote.service.BillingService
import com.example.clicknote.service.impl.BillingServiceImpl
import com.example.clicknote.service.impl.SubscriptionServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BindingModule {

    @Binds
    @Singleton
    abstract fun bindBillingService(
        impl: BillingServiceImpl
    ): BillingService

    @Binds
    @Singleton
    abstract fun bindSubscriptionService(
        impl: SubscriptionServiceImpl
    ): SubscriptionService
} 