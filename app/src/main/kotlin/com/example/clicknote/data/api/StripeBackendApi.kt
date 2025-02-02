package com.example.clicknote.data.api

interface StripeBackendApi {
    suspend fun createSubscription(request: CreateSubscriptionRequest): CreateSubscriptionResponse
    suspend fun cancelSubscription()
}

data class CreateSubscriptionRequest(
    val priceId: String,
    val paymentMethodId: String
)

data class CreateSubscriptionResponse(
    val subscriptionId: String,
    val status: String,
    val currentPeriodEnd: String,
    val clientSecret: String?
) 