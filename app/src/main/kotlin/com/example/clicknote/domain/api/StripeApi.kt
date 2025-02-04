package com.example.clicknote.domain.api

import com.example.clicknote.domain.model.stripe.*
import retrofit2.http.*

interface StripeApi {
    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CreateCustomerResponse

    @POST("ephemeral-keys")
    suspend fun createEphemeralKey(@Body request: GetEphemeralKeyRequest): GetEphemeralKeyResponse

    @POST("payment-intents")
    suspend fun createPaymentIntent(@Body request: CreatePaymentIntentRequest): CreatePaymentIntentResponse

    @POST("payment-methods/attach")
    suspend fun attachPaymentMethod(@Body request: UpdatePaymentMethodRequest)

    @GET("subscription-plans")
    suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse
} 