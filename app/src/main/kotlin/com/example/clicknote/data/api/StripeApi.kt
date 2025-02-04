package com.example.clicknote.data.api

import com.example.clicknote.data.api.model.*
import retrofit2.http.*

interface StripeApi {
    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CreateCustomerResponse

    @POST("subscriptions")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): CreateSubscriptionResponse

    @POST("ephemeral-keys")
    suspend fun createEphemeralKey(@Body request: GetEphemeralKeyRequest): GetEphemeralKeyResponse

    @POST("payment-intents")
    suspend fun createPaymentIntent(@Body request: CreatePaymentIntentRequest): CreatePaymentIntentResponse

    @POST("payment-methods/attach")
    suspend fun attachPaymentMethod(@Body request: UpdatePaymentMethodRequest)

    @GET("subscription-plans")
    suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse

    @POST("subscriptions/{subscriptionId}/cancel")
    suspend fun cancelSubscription(@Path("subscriptionId") subscriptionId: String): CancelSubscriptionResponse
} 