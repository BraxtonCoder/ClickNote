package com.example.clicknote.domain.model.stripe

import com.google.gson.annotations.SerializedName

data class CreateCustomerRequest(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String? = null
)

data class CreateCustomerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("email") val email: String,
    @SerializedName("created") val created: Long
)

data class GetEphemeralKeyRequest(
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("api_version") val apiVersion: String
)

data class GetEphemeralKeyResponse(
    @SerializedName("id") val id: String,
    @SerializedName("object") val objectType: String,
    @SerializedName("associated_objects") val associatedObjects: List<AssociatedObject>,
    @SerializedName("created") val created: Long,
    @SerializedName("expires") val expires: Long,
    @SerializedName("livemode") val livemode: Boolean,
    @SerializedName("secret") val secret: String
)

data class AssociatedObject(
    @SerializedName("id") val id: String,
    @SerializedName("type") val type: String
)

data class CreatePaymentIntentRequest(
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("customer_id") val customerId: String,
    @SerializedName("payment_method_types") val paymentMethodTypes: List<String> = listOf("card")
)

data class CreatePaymentIntentResponse(
    @SerializedName("id") val id: String,
    @SerializedName("client_secret") val clientSecret: String,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("status") val status: String
)

data class UpdatePaymentMethodRequest(
    @SerializedName("payment_method_id") val paymentMethodId: String,
    @SerializedName("customer_id") val customerId: String
)

data class GetSubscriptionPlansResponse(
    @SerializedName("data") val data: List<StripePlan>
)

data class StripePlan(
    @SerializedName("id") val id: String,
    @SerializedName("object") val objectType: String,
    @SerializedName("active") val active: Boolean,
    @SerializedName("amount") val amount: Int,
    @SerializedName("currency") val currency: String,
    @SerializedName("interval") val interval: String,
    @SerializedName("interval_count") val intervalCount: Int,
    @SerializedName("nickname") val nickname: String?,
    @SerializedName("product") val product: String
) 