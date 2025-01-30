package com.example.clicknote.di

import com.example.clicknote.data.api.StripeApi
import com.example.clicknote.data.api.StripeApiImpl
import com.example.clicknote.data.local.SubscriptionDao
import com.example.clicknote.data.preferences.UserPreferences
import com.example.clicknote.data.preferences.UserPreferencesImpl
import com.example.clicknote.data.repository.SubscriptionRepositoryImpl
import com.example.clicknote.domain.repository.SubscriptionRepository
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
    abstract fun bindSubscriptionRepository(
        impl: SubscriptionRepositoryImpl
    ): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindUserPreferences(
        impl: UserPreferencesImpl
    ): UserPreferences

    @Binds
    @Singleton
    abstract fun bindStripeApi(
        impl: StripeApiImpl
    ): StripeApi
}