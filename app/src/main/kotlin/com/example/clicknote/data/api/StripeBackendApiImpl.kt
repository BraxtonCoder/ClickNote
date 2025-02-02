package com.example.clicknote.data.api

import javax.inject.Inject

class StripeBackendApiImpl @Inject constructor(
    private val stripeService: StripeService
) : StripeBackendApi {
    override suspend fun createSubscription(request: CreateSubscriptionRequest): CreateSubscriptionResponse {
        // TODO: Implement actual backend API call
        return CreateSubscriptionResponse(
            subscriptionId = "sub_123",
            status = "active",
            currentPeriodEnd = "2024-12-31T23:59:59",
            clientSecret = null
        )
    }

    override suspend fun cancelSubscription() {
        // TODO: Implement actual backend API call
    }
} 