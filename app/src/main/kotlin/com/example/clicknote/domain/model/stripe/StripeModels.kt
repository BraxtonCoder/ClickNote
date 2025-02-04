package com.example.clicknote.domain.model.stripe

import com.google.gson.annotations.SerializedName

data class CreateCustomerRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("metadata") val metadata: Map<String, String> = emptyMap()
)

data class CreateCustomerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("phone") val phone: String?,
    @SerializedName("created") val created: Long,
    @SerializedName("metadata") val metadata: Map<String, String>
)

data class CreateSubscriptionRequest(
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("price_id") val priceId: String,
    @SerializedName("payment_method_id") val paymentMethodId: String? = null,
    @SerializedName("trial_period_days") val trialPeriodDays: Int? = null
)

data class CreateSubscriptionResponse(
    @SerializedName("id") val id: String,
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("status") val status: String,
    @SerializedName("current_period_start") val currentPeriodStart: Long,
    @SerializedName("current_period_end") val currentPeriodEnd: Long,
    @SerializedName("cancel_at_period_end") val cancelAtPeriodEnd: Boolean
)

data class GetEphemeralKeyRequest(
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("api_version") val apiVersion: String
)

data class GetEphemeralKeyResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val object: String,
    @SerializedName("associated_objects") val associatedObjects: List<AssociatedObject>,
    @SerializedName("created") val created: Long,
    @SerializedName("expires") val expires: Long,
    @SerializedName("secret") val secret: String
)

data class AssociatedObject(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String
)

data class CreatePaymentIntentRequest(
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("customer_id") val customerId: String? = null,
    @SerializedName("payment_method_types") val paymentMethodTypes: List<String> = listOf("card"),
    @SerializedName("metadata") val metadata: Map<String, String> = emptyMap()
)

data class CreatePaymentIntentResponse(
    @SerializedName("id") val id: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("status") val status: String,
    @SerializedName("client_secret") val clientSecret: String
)

data class UpdatePaymentMethodRequest(
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("customer_id") val customerId: String
)

data class GetSubscriptionPlansResponse(
    @SerializedName("data") val plans: List<SubscriptionPlan>
)

data class SubscriptionPlan(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("currency") val currency: String,
    @SerializedName("interval") val interval: String,
    @SerializedName("interval_count") val intervalCount: Int,
    @SerializedName("metadata") val metadata: Map<String, String>
) 