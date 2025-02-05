package com.example.clicknote.domain.service

import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentIntent

interface StripeApi {
    suspend fun createPaymentIntent(
        amount: Long,
        currency: String = "gbp",
        customerId: String? = null
    ): Result<PaymentIntent>

    suspend fun confirmPaymentIntent(
        params: ConfirmPaymentIntentParams
    ): Result<PaymentIntent>

    suspend fun createCustomer(
        email: String,
        name: String? = null,
        paymentMethod: PaymentMethod? = null
    ): Result<String>

    suspend fun attachPaymentMethodToCustomer(
        paymentMethodId: String,
        customerId: String
    ): Result<Unit>

    suspend fun createSubscription(
        customerId: String,
        priceId: String,
        paymentMethodId: String? = null
    ): Result<String>

    suspend fun cancelSubscription(
        subscriptionId: String
    ): Result<Unit>

    suspend fun getSubscriptionStatus(
        subscriptionId: String
    ): Result<String>

    suspend fun updateDefaultPaymentMethod(
        customerId: String,
        paymentMethodId: String
    ): Result<Unit>
} 