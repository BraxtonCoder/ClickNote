package com.example.clicknote.data.api.model

import com.example.clicknote.domain.model.SubscriptionPlan
import com.google.gson.annotations.SerializedName

data class CreateCustomerRequest(
    @SerializedName("email") val email: String
)

data class CreateCustomerResponse(
    @SerializedName("customer_id") val customerId: String
)

data class GetEphemeralKeyRequest(
    @SerializedName("customer_id") val customerId: String
)

data class GetEphemeralKeyResponse(
    @SerializedName("ephemeral_key") val ephemeralKey: String
)

data class CreatePaymentIntentRequest(
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("customer_id") val customerId: String
)

data class CreatePaymentIntentResponse(
    @SerializedName("client_secret") val clientSecret: String
)

data class UpdatePaymentMethodRequest(
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("payment_method_id") val paymentMethodId: String
)

data class GetSubscriptionPlansResponse(
    @SerializedName("plans") val plans: List<SubscriptionPlan>
)

data class CreateSubscriptionRequest(
    @SerializedName("price_id") val priceId: String,
    @SerializedName("payment_method_id") val paymentMethodId: String
)

data class CreateSubscriptionResponse(
    @SerializedName("subscription_id") val subscriptionId: String,
    @SerializedName("status") val status: String,
    @SerializedName("current_period_end") val currentPeriodEnd: String,
    @SerializedName("client_secret") val clientSecret: String?
)

data class CancelSubscriptionRequest(
    @SerializedName("subscription_id")
    val subscriptionId: String
)

data class CancelSubscriptionResponse(
    val status: String
) 