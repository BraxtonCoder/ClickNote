package com.example.clicknote.data.api.service

import com.example.clicknote.data.api.model.*
import retrofit2.http.*

interface StripeService {
    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CreateCustomerResponse

    @POST("ephemeral-keys")
    suspend fun getEphemeralKey(@Body request: GetEphemeralKeyRequest): GetEphemeralKeyResponse

    @POST("payment-intents")
    suspend fun createPaymentIntent(@Body request: CreatePaymentIntentRequest): CreatePaymentIntentResponse

    @POST("payment-methods")
    suspend fun updatePaymentMethod(@Body request: UpdatePaymentMethodRequest)

    @GET("subscription-plans")
    suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse

    @POST("create-subscription")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): CreateSubscriptionResponse

    @GET("subscription/{subscriptionId}")
    suspend fun getSubscription(@Path("subscriptionId") subscriptionId: String): CreateSubscriptionResponse

    @POST("cancel-subscription")
    suspend fun cancelSubscription(@Body request: CancelSubscriptionRequest): CancelSubscriptionResponse
} 