package com.example.clicknote.di

import com.example.clicknote.service.api.StripeApi
import com.example.clicknote.service.api.impl.StripeApiImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ExternalServiceModule {

    @Provides
    @Singleton
    fun provideStripeApi(): StripeApi {
        return StripeApiImpl()
    }
} 