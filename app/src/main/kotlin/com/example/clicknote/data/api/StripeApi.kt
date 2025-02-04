package com.example.clicknote.data.api

import com.example.clicknote.data.api.model.*
import retrofit2.http.*

interface StripeApi {
    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CreateCustomerResponse

    @POST("ephemeral_keys")
    suspend fun createEphemeralKey(@Body request: GetEphemeralKeyRequest): GetEphemeralKeyResponse

    @POST("payment_intents")
    suspend fun createPaymentIntent(@Body request: CreatePaymentIntentRequest): CreatePaymentIntentResponse

    @POST("payment_methods/attach")
    suspend fun attachPaymentMethod(@Body request: UpdatePaymentMethodRequest)

    @POST("subscriptions")
    suspend fun createSubscription(@Body request: CreateSubscriptionRequest): CreateSubscriptionResponse

    @GET("subscription_plans")
    suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse
} 