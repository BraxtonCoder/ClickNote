package com.example.clicknote.data.api

import androidx.activity.ComponentActivity
import com.example.clicknote.data.api.model.*
import com.example.clicknote.data.api.service.StripeBackendApi
import com.example.clicknote.data.api.service.StripeService
import com.example.clicknote.domain.model.SubscriptionPlan
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import dagger.hilt.android.qualifiers.ActivityContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import retrofit2.Retrofit

@Singleton
class StripeApiImpl @Inject constructor(
    private val retrofit: Retrofit
) : StripeApi {
    private val api = retrofit.create(StripeApi::class.java)

    override suspend fun createCustomer(request: CreateCustomerRequest): CreateCustomerResponse {
        return api.createCustomer(request)
    }

    override suspend fun createSubscription(request: CreateSubscriptionRequest): CreateSubscriptionResponse {
        return api.createSubscription(request)
    }

    override suspend fun createEphemeralKey(request: GetEphemeralKeyRequest): GetEphemeralKeyResponse {
        return api.createEphemeralKey(request)
    }

    override suspend fun createPaymentIntent(request: CreatePaymentIntentRequest): CreatePaymentIntentResponse {
        return api.createPaymentIntent(request)
    }

    override suspend fun attachPaymentMethod(request: UpdatePaymentMethodRequest) {
        api.attachPaymentMethod(request)
    }

    override suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse {
        return api.getSubscriptionPlans()
    }
}

interface StripeService {
    @POST("customers")
    suspend fun createCustomer(@Body request: CreateCustomerRequest): CreateCustomerResponse

    @POST("ephemeral-keys")
    suspend fun getEphemeralKey(@Body request: GetEphemeralKeyRequest): GetEphemeralKeyResponse

    @POST("payment-intents")
    suspend fun createPaymentIntent(@Body request: CreatePaymentIntentRequest): CreatePaymentIntentResponse

    @POST("payment-methods")
    suspend fun updatePaymentMethod(@Body request: UpdatePaymentMethodRequest)

    @GET("subscription-plans")
    suspend fun getSubscriptionPlans(): GetSubscriptionPlansResponse
}

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

private data class CreateCustomerRequest(val email: String)
private data class CreateCustomerResponse(val customerId: String)
private data class GetEphemeralKeyRequest(val customerId: String)
private data class GetEphemeralKeyResponse(val ephemeralKey: String)
private data class CreatePaymentIntentRequest(val amount: Int, val currency: String, val customerId: String)
private data class CreatePaymentIntentResponse(val clientSecret: String)
private data class UpdatePaymentMethodRequest(val customerId: String, val paymentMethodId: String)
private data class GetSubscriptionPlansResponse(val plans: List<SubscriptionPlan>) 