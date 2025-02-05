package com.example.clicknote.data.api

import retrofit2.Response
import retrofit2.http.*

interface StripeService {
    @POST("v1/payment_intents")
    suspend fun createPaymentIntent(
        @Body request: CreatePaymentIntentRequest
    ): Response<PaymentIntent>

    @POST("v1/customers")
    suspend fun createCustomer(
        @Body request: CreateCustomerRequest
    ): Response<Customer>

    @GET("v1/customers/{customerId}")
    suspend fun getCustomer(
        @Path("customerId") customerId: String
    ): Response<Customer>

    @POST("v1/subscriptions")
    suspend fun createSubscription(
        @Body request: CreateSubscriptionRequest
    ): Response<Subscription>

    @GET("v1/subscriptions/{subscriptionId}")
    suspend fun getSubscription(
        @Path("subscriptionId") subscriptionId: String
    ): Response<Subscription>
}

data class CreatePaymentIntentRequest(
    val amount: Long,
    val currency: String = "gbp",
    val customerId: String? = null,
    val paymentMethodTypes: List<String> = listOf("card")
)

data class PaymentIntent(
    val id: String,
    val amount: Long,
    val clientSecret: String,
    val status: String
)

data class CreateCustomerRequest(
    val email: String,
    val name: String? = null,
    val description: String? = null
)

data class Customer(
    val id: String,
    val email: String,
    val name: String?,
    val description: String?
)

data class CreateSubscriptionRequest(
    val customerId: String,
    val priceId: String,
    val paymentBehavior: String = "default_incomplete"
)

data class Subscription(
    val id: String,
    val customerId: String,
    val status: String,
    val currentPeriodEnd: Long
) 