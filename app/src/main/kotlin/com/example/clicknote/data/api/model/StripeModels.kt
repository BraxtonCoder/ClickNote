package com.example.clicknote.data.api.model

import com.example.clicknote.domain.model.SubscriptionPlan
import com.google.gson.annotations.SerializedName

data class CreateCustomerRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("metadata") val metadata: Map<String, String>? = null
)

data class CreateCustomerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String,
    @SerializedName("created") val created: Long
)

data class CreateSubscriptionRequest(
    @SerializedName("customer") val customerId: String,
    @SerializedName("price") val priceId: String,
    @SerializedName("payment_method") val paymentMethodId: String
)

data class CreateSubscriptionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("customer") val customerId: String,
    @SerializedName("status") val status: String,
    @SerializedName("current_period_end") val currentPeriodEnd: Long
)

data class GetEphemeralKeyRequest(
    @SerializedName("customer") val customerId: String,
    @SerializedName("stripe_version") val stripeVersion: String
)

data class GetEphemeralKeyResponse(
    @SerializedName("id") val id: String,
    @SerializedName("secret") val secret: String,
    @SerializedName("expires") val expires: Long
)

data class CreatePaymentIntentRequest(
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("customer") val customerId: String,
    @SerializedName("payment_method_types") val paymentMethodTypes: List<String>
)

data class CreatePaymentIntentResponse(
    @SerializedName("id") val id: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("status") val status: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String
)

data class UpdatePaymentMethodRequest(
    @SerializedName("payment_method") val paymentMethodId: String,
    @SerializedName("customer") val customerId: String
)

data class GetSubscriptionPlansResponse(
    @SerializedName("plans") val plans: List<SubscriptionPlan>
)

data class SubscriptionPlan(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("interval") val interval: String,
    @SerializedName("features") val features: List<String>
)

data class CancelSubscriptionRequest(
    @SerializedName("subscription_id")
    val subscriptionId: String
)

data class CancelSubscriptionResponse(
    val status: String
) 