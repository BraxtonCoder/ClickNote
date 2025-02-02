package com.example.clicknote.di

import com.example.clicknote.domain.service.BillingService
import com.example.clicknote.domain.service.SubscriptionService
import com.example.clicknote.service.impl.BillingServiceImpl
import com.example.clicknote.service.impl.SubscriptionServiceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SubscriptionModule {

    @Binds
    @Singleton
    abstract fun bindBillingService(impl: BillingServiceImpl): BillingService

    @Binds
    @Singleton
    abstract fun bindSubscriptionService(impl: SubscriptionServiceImpl): SubscriptionService
}