package com.example.clicknote.data.api

import com.example.clicknote.data.api.model.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StripeBackendApiImpl @Inject constructor(
    private val stripeService: StripeBackendApi
) {
    suspend fun createSubscription(request: CreateSubscriptionRequest): CreateSubscriptionResponse =
        stripeService.createSubscription(request)

    suspend fun cancelSubscription(subscriptionId: String): CancelSubscriptionResponse =
        stripeService.cancelSubscription(subscriptionId)

    suspend fun createCustomer(request: CreateCustomerRequest): CreateCustomerResponse =
        stripeService.createCustomer(request)

    suspend fun createPaymentIntent(request: CreatePaymentIntentRequest): CreatePaymentIntentResponse =
        stripeService.createPaymentIntent(request)

    suspend fun createEphemeralKey(request: GetEphemeralKeyRequest): GetEphemeralKeyResponse =
        stripeService.createEphemeralKey(request)

    suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse =
        stripeService.getSubscriptionPlans()
} 