package com.example.clicknote.service.api.impl

import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.service.api.StripeApi
import retrofit2.Retrofit
import retrofit2.http.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StripeApiImpl @Inject constructor(
    private val retrofit: Retrofit
) : StripeApi {
    private val api = retrofit.create(StripeService::class.java)

    override suspend fun createCustomer(email: String): Result<String> = runCatching {
        api.createCustomer(CreateCustomerRequest(email)).customerId
    }

    override suspend fun createSubscription(customerId: String, planId: String): Result<String> = runCatching {
        api.createSubscription(CreateSubscriptionRequest(customerId, planId)).subscriptionId
    }

    override suspend fun getCustomerEphemeralKey(customerId: String): Result<String> = runCatching {
        api.getEphemeralKey(GetEphemeralKeyRequest(customerId)).ephemeralKey
    }

    override suspend fun getPaymentIntent(amount: Int, currency: String, customerId: String): Result<String> = runCatching {
        api.createPaymentIntent(CreatePaymentIntentRequest(amount, currency, customerId)).clientSecret
    }

    override suspend fun cancelSubscription(subscriptionId: String): Result<Unit> = runCatching {
        api.cancelSubscription(subscriptionId)
    }

    override suspend fun updatePaymentMethod(customerId: String, paymentMethodId: String): Result<Unit> = runCatching {
        api.updatePaymentMethod(UpdatePaymentMethodRequest(customerId, paymentMethodId))
    }

    override suspend fun getSubscriptionPlans(): Result<List<SubscriptionPlan>> = runCatching {
        api.getSubscriptionPlans().plans
    }

    private interface StripeService {
        @POST("customers")
        suspend fun createCustomer(@Body request: CreateCustomerRequest): CreateCustomerResponse

        @POST("subscriptions")
        suspend fun createSubscription(@Body request: CreateSubscriptionRequest): CreateSubscriptionResponse

        @POST("ephemeral-keys")
        suspend fun getEphemeralKey(@Body request: GetEphemeralKeyRequest): GetEphemeralKeyResponse

        @POST("payment-intents")
        suspend fun createPaymentIntent(@Body request: CreatePaymentIntentRequest): CreatePaymentIntentResponse

        @DELETE("subscriptions/{subscriptionId}")
        suspend fun cancelSubscription(@Path("subscriptionId") subscriptionId: String)

        @POST("payment-methods")
        suspend fun updatePaymentMethod(@Body request: UpdatePaymentMethodRequest)

        @GET("subscription-plans")
        suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse
    }

    private data class CreateCustomerRequest(val email: String)
    private data class CreateCustomerResponse(val customerId: String)
    private data class CreateSubscriptionRequest(val customerId: String, val planId: String)
    private data class CreateSubscriptionResponse(val subscriptionId: String)
    private data class GetEphemeralKeyRequest(val customerId: String)
    private data class GetEphemeralKeyResponse(val ephemeralKey: String)
    private data class CreatePaymentIntentRequest(val amount: Int, val currency: String, val customerId: String)
    private data class CreatePaymentIntentResponse(val clientSecret: String)
    private data class UpdatePaymentMethodRequest(val customerId: String, val paymentMethodId: String)
    private data class GetSubscriptionPlansResponse(val plans: List<SubscriptionPlan>)
} 