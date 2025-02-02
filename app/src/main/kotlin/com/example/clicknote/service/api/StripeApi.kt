package com.example.clicknote.service.api

import com.example.clicknote.domain.model.SubscriptionPlan

interface StripeApi {
    suspend fun createCustomer(email: String): Result<String>
    suspend fun createSubscription(
        customerId: String,
        planId: String
    ): Result<String>
    
    suspend fun getCustomerEphemeralKey(
        customerId: String
    ): Result<String>
    
    suspend fun getPaymentIntent(
        amount: Int,
        currency: String = "gbp",
        customerId: String
    ): Result<String>
    
    suspend fun cancelSubscription(
        subscriptionId: String
    ): Result<Unit>
    
    suspend fun updatePaymentMethod(
        customerId: String,
        paymentMethodId: String
    ): Result<Unit>
    
    suspend fun getSubscriptionPlans(): Result<List<SubscriptionPlan>>
} 