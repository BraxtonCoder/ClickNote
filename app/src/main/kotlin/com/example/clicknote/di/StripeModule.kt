package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.data.api.StripeApiImpl
import com.example.clicknote.data.api.service.StripeBackendApi
import com.example.clicknote.data.api.service.StripeService
import com.example.clicknote.domain.api.StripeApi
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object StripeModule {

    @Provides
    @Singleton
    fun provideStripe(
        @ApplicationContext context: Context
    ): Stripe {
        return Stripe(context, PaymentConfiguration.getInstance(context).publishableKey)
    }

    @Provides
    @Singleton
    fun provideStripeApi(
        impl: StripeApiImpl
    ): StripeApi = impl

    @Provides
    @Singleton
    fun provideStripeBackendApi(
        stripeService: StripeService
    ): StripeBackendApi {
        // Implement your StripeBackendApi here
        return object : StripeBackendApi {
            override suspend fun createSubscription(request: com.example.clicknote.data.api.model.CreateSubscriptionRequest) =
                stripeService.createSubscription(request)
            
            override suspend fun cancelSubscription() {
                stripeService.cancelSubscription(com.example.clicknote.data.api.model.CancelSubscriptionRequest(""))
            }
        }
    }
} 