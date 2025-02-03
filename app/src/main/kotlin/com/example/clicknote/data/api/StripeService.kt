package com.example.clicknote.data.api

import com.example.clicknote.domain.model.SubscriptionPlan
import retrofit2.http.*

interface StripeService {
    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CreateCustomerResponse

    @POST("ephemeral-keys")
    suspend fun getEphemeralKey(@Body request: GetEphemeralKeyRequest): GetEphemeralKeyResponse

    @POST("create-payment-intent")
    suspend fun createPaymentIntent(@Body request: PaymentIntentRequest): PaymentIntentResponse

    @POST("payment-methods")
    suspend fun updatePaymentMethod(@Body request: UpdatePaymentMethodRequest)

    @GET("subscription-plans")
    suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse

    @POST("create-subscription")
    suspend fun createSubscription(@Body request: SubscriptionRequest): SubscriptionResponse

    @GET("subscription/{subscriptionId}")
    suspend fun getSubscription(@Path("subscriptionId") subscriptionId: String): SubscriptionResponse

    @POST("cancel-subscription")
    suspend fun cancelSubscription(@Body request: CancelSubscriptionRequest): CancelSubscriptionResponse
}

data class CreateCustomerRequest(val email: String)
data class CreateCustomerResponse(val customerId: String)
data class GetEphemeralKeyRequest(val customerId: String)
data class GetEphemeralKeyResponse(val ephemeralKey: String)
data class PaymentIntentRequest(val amount: Int, val currency: String, val customerId: String)
data class PaymentIntentResponse(val clientSecret: String)
data class UpdatePaymentMethodRequest(val customerId: String, val paymentMethodId: String)
data class GetSubscriptionPlansResponse(val plans: List<SubscriptionPlan>)
data class SubscriptionRequest(val planId: String, val customerId: String)
data class SubscriptionResponse(val subscriptionId: String)
data class CancelSubscriptionRequest(val subscriptionId: String)
data class CancelSubscriptionResponse(val status: String) 