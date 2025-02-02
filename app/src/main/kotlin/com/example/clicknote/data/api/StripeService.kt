package com.example.clicknote.data.api

import com.example.clicknote.domain.model.SubscriptionPlan
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
}

data class CreateCustomerRequest(val email: String)
data class CreateCustomerResponse(val customerId: String)
data class GetEphemeralKeyRequest(val customerId: String)
data class GetEphemeralKeyResponse(val ephemeralKey: String)
data class CreatePaymentIntentRequest(val amount: Int, val currency: String, val customerId: String)
data class CreatePaymentIntentResponse(val clientSecret: String)
data class UpdatePaymentMethodRequest(val customerId: String, val paymentMethodId: String)
data class GetSubscriptionPlansResponse(val plans: List<SubscriptionPlan>) 