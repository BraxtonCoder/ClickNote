package com.example.clicknote.domain.api

import com.example.clicknote.domain.model.stripe.*

interface StripeApi {
    suspend fun createCustomer(request: CreateCustomerRequest): CreateCustomerResponse
    suspend fun createSubscription(request: CreateSubscriptionRequest): CreateSubscriptionResponse
    suspend fun createEphemeralKey(request: GetEphemeralKeyRequest): GetEphemeralKeyResponse
    suspend fun createPaymentIntent(request: CreatePaymentIntentRequest): CreatePaymentIntentResponse
    suspend fun attachPaymentMethod(request: UpdatePaymentMethodRequest): Unit
    suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse
} 