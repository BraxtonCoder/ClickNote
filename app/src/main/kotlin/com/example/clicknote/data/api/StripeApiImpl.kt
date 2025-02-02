package com.example.clicknote.data.api

import com.example.clicknote.domain.model.SubscriptionPlan
import com.stripe.android.Stripe
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentMethod
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StripeApiImpl @Inject constructor(
    private val stripe: Stripe,
    private val stripeBackendApi: StripeBackendApi,
    private val stripeService: StripeService
) : StripeApi {

    override suspend fun createCustomer(email: String): Result<String> = runCatching {
        stripeService.createCustomer(CreateCustomerRequest(email)).customerId
    }

    override suspend fun createSubscription(priceId: String, paymentMethodId: String): StripeSubscription = withContext(Dispatchers.IO) {
        // Create subscription on backend
        val response = stripeBackendApi.createSubscription(
            CreateSubscriptionRequest(
                priceId = priceId,
                paymentMethodId = paymentMethodId
            )
        )

        // Confirm payment if required
        if (response.clientSecret != null) {
            val paymentMethod = stripe.retrievePaymentMethod(paymentMethodId)
            val params = ConfirmPaymentIntentParams.createWithPaymentMethodId(
                paymentMethodId = paymentMethodId,
                clientSecret = response.clientSecret,
                paymentMethodType = PaymentMethod.Type.Card
            )
            stripe.confirmPayment(params)
        }

        return@withContext StripeSubscription(
            id = response.subscriptionId,
            status = response.status,
            currentPeriodEnd = LocalDateTime.parse(response.currentPeriodEnd)
        )
    }

    override suspend fun getCustomerEphemeralKey(customerId: String): Result<String> = runCatching {
        stripeService.getEphemeralKey(GetEphemeralKeyRequest(customerId)).ephemeralKey
    }

    override suspend fun getPaymentIntent(amount: Int, currency: String, customerId: String): Result<String> = runCatching {
        stripeService.createPaymentIntent(CreatePaymentIntentRequest(amount, currency, customerId)).clientSecret
    }

    override suspend fun cancelSubscription() = withContext(Dispatchers.IO) {
        stripeBackendApi.cancelSubscription()
    }

    override suspend fun updatePaymentMethod(customerId: String, paymentMethodId: String): Result<Unit> = runCatching {
        stripeService.updatePaymentMethod(UpdatePaymentMethodRequest(customerId, paymentMethodId))
    }

    override suspend fun getSubscriptionPlans(): Result<List<SubscriptionPlan>> = runCatching {
        stripeService.getSubscriptionPlans().plans
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