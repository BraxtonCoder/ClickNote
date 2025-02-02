package com.example.clicknote.data.api

import com.example.clicknote.domain.model.SubscriptionPlan
import java.time.LocalDateTime

interface StripeApi {
    suspend fun createCustomer(email: String): Result<String>
    suspend fun createSubscription(priceId: String, paymentMethodId: String): StripeSubscription
    suspend fun getCustomerEphemeralKey(customerId: String): Result<String>
    suspend fun getPaymentIntent(amount: Int, currency: String, customerId: String): Result<String>
    suspend fun cancelSubscription()
    suspend fun updatePaymentMethod(customerId: String, paymentMethodId: String): Result<Unit>
    suspend fun getSubscriptionPlans(): Result<List<SubscriptionPlan>>
}

data class StripeSubscription(
    val id: String,
    val status: String,
    val currentPeriodEnd: LocalDateTime
) 