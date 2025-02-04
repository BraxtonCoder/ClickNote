package com.example.clicknote.data.api

import com.example.clicknote.data.api.model.*
import retrofit2.http.*

interface StripeBackendApi {
    @POST("stripe/create-subscription")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): CreateSubscriptionResponse

    @POST("stripe/cancel-subscription/{subscriptionId}")
    suspend fun cancelSubscription(@Path("subscriptionId") subscriptionId: String): CancelSubscriptionResponse

    @POST("stripe/create-customer")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CreateCustomerResponse

    @POST("stripe/create-payment-intent")
    suspend fun createPaymentIntent(@Body request: CreatePaymentIntentRequest): CreatePaymentIntentResponse

    @POST("stripe/create-ephemeral-key")
    suspend fun createEphemeralKey(@Body request: GetEphemeralKeyRequest): GetEphemeralKeyResponse

    @GET("stripe/subscription-plans")
    suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse
} 