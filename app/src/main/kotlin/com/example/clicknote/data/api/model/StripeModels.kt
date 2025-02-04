package com.example.clicknote.data.api.model

import com.example.clicknote.domain.model.SubscriptionPlan
import com.google.gson.annotations.SerializedName

data class CreateCustomerRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String
)

data class CreateCustomerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String
)

data class GetEphemeralKeyRequest(
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("api_version") val apiVersion: String
)

data class GetEphemeralKeyResponse(
    @SerializedName("id") val id: String,
    @SerializedName("secret") val secret: String,
    @SerializedName("expires") val expires: Long
)

data class CreatePaymentIntentRequest(
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("payment_method_id") val paymentMethodId: String?
)

data class CreatePaymentIntentResponse(
    @SerializedName("id") val id: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("status") val status: String
)

data class UpdatePaymentMethodRequest(
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("payment_method_id") val paymentMethodId: String
)

data class GetSubscriptionPlansResponse(
    @SerializedName("plans") val plans: List<SubscriptionPlan>
)

data class CreateSubscriptionRequest(
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("price_id") val priceId: String,
    @SerializedName("payment_method_id") val paymentMethodId: String
)

data class CreateSubscriptionResponse(
    @SerializedName("subscription_id") val subscriptionId: String,
    @SerializedName("status") val status: String,
    @SerializedName("current_period_end") val currentPeriodEnd: Long
)

data class CancelSubscriptionRequest(
    @SerializedName("subscription_id")
    val subscriptionId: String
)

data class CancelSubscriptionResponse(
    val status: String
)

data class SubscriptionPlan(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("interval") val interval: String,
    @SerializedName("interval_count") val intervalCount: Int
) 