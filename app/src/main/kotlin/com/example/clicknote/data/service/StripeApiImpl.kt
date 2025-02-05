package com.example.clicknote.data.service

import com.example.clicknote.BuildConfig
import com.example.clicknote.domain.service.StripeApi
import com.stripe.android.model.PaymentMethod
import com.stripe.android.model.ConfirmPaymentIntentParams
import com.stripe.android.model.PaymentIntent
import com.stripe.android.model.StripeIntent
import retrofit2.Retrofit
import retrofit2.http.*
import javax.inject.Inject
import javax.inject.Singleton
import java.util.Date

@Singleton
class StripeApiImpl @Inject constructor(
    private val retrofit: Retrofit
) : StripeApi {

    private val api = retrofit.create(StripeApiService::class.java)

    override suspend fun createPaymentIntent(
        amount: Long,
        currency: String,
        customerId: String?
    ): Result<PaymentIntent> = runCatching {
        val request = CreatePaymentIntentRequest(
            amount = amount,
            currency = currency,
            customerId = customerId
        )
        val response = api.createPaymentIntent(request)
        PaymentIntent(
            id = response.id,
            clientSecret = response.clientSecret,
            amount = response.amount,
            currency = response.currency,
            status = parseStatus(response.status),
            created = response.created,
            isLiveMode = response.livemode,
            paymentMethodTypes = response.paymentMethodTypes,
            countryCode = response.countryCode ?: "",
            unactivatedPaymentMethods = response.paymentMethodTypes
        )
    }

    override suspend fun confirmPaymentIntent(
        params: ConfirmPaymentIntentParams
    ): Result<PaymentIntent> = runCatching {
        val request = ConfirmPaymentIntentRequest(params)
        val response = api.confirmPaymentIntent(params.paymentMethodId ?: "", request)
        PaymentIntent(
            id = response.id,
            clientSecret = response.clientSecret,
            amount = response.amount,
            currency = response.currency,
            status = parseStatus(response.status),
            created = response.created,
            isLiveMode = response.livemode,
            paymentMethodTypes = response.paymentMethodTypes,
            countryCode = response.countryCode ?: "",
            unactivatedPaymentMethods = response.paymentMethodTypes
        )
    }

    private fun parseStatus(status: String): StripeIntent.Status {
        return when (status.lowercase()) {
            "requires_payment_method" -> StripeIntent.Status.RequiresPaymentMethod
            "requires_confirmation" -> StripeIntent.Status.RequiresConfirmation
            "requires_action" -> StripeIntent.Status.RequiresAction
            "processing" -> StripeIntent.Status.Processing
            "succeeded" -> StripeIntent.Status.Succeeded
            "canceled" -> StripeIntent.Status.Canceled
            else -> StripeIntent.Status.Canceled
        }
    }

    override suspend fun createCustomer(
        email: String,
        name: String?,
        paymentMethod: PaymentMethod?
    ): Result<String> = runCatching {
        val request = CreateCustomerRequest(
            email = email,
            name = name,
            paymentMethodId = paymentMethod?.id
        )
        api.createCustomer(request).id
    }

    override suspend fun attachPaymentMethodToCustomer(
        paymentMethodId: String,
        customerId: String
    ): Result<Unit> = runCatching {
        api.attachPaymentMethod(paymentMethodId, AttachPaymentMethodRequest(customerId))
    }

    override suspend fun createSubscription(
        customerId: String,
        priceId: String,
        paymentMethodId: String?
    ): Result<String> = runCatching {
        val request = CreateSubscriptionRequest(
            customerId = customerId,
            priceId = priceId,
            paymentMethodId = paymentMethodId
        )
        api.createSubscription(request).id
    }

    override suspend fun cancelSubscription(subscriptionId: String): Result<Unit> = runCatching {
        api.cancelSubscription(subscriptionId)
    }

    override suspend fun getSubscriptionStatus(subscriptionId: String): Result<String> = runCatching {
        api.getSubscription(subscriptionId).status
    }

    override suspend fun updateDefaultPaymentMethod(
        customerId: String,
        paymentMethodId: String
    ): Result<Unit> = runCatching {
        api.updateDefaultPaymentMethod(
            customerId,
            UpdateDefaultPaymentMethodRequest(paymentMethodId)
        )
    }

    private interface StripeApiService {
        @POST("v1/payment_intents")
        suspend fun createPaymentIntent(
            @Body request: CreatePaymentIntentRequest
        ): StripePaymentIntent

        @POST("v1/payment_intents/{id}/confirm")
        suspend fun confirmPaymentIntent(
            @Path("id") paymentIntentId: String,
            @Body request: ConfirmPaymentIntentRequest
        ): StripePaymentIntent

        @POST("v1/customers")
        suspend fun createCustomer(
            @Body request: CreateCustomerRequest
        ): StripeCustomer

        @POST("v1/payment_methods/{id}/attach")
        suspend fun attachPaymentMethod(
            @Path("id") paymentMethodId: String,
            @Body request: AttachPaymentMethodRequest
        )

        @POST("v1/subscriptions")
        suspend fun createSubscription(
            @Body request: CreateSubscriptionRequest
        ): StripeSubscription

        @DELETE("v1/subscriptions/{id}")
        suspend fun cancelSubscription(
            @Path("id") subscriptionId: String
        )

        @GET("v1/subscriptions/{id}")
        suspend fun getSubscription(
            @Path("id") subscriptionId: String
        ): StripeSubscription

        @POST("v1/customers/{id}")
        suspend fun updateDefaultPaymentMethod(
            @Path("id") customerId: String,
            @Body request: UpdateDefaultPaymentMethodRequest
        )
    }

    private data class CreatePaymentIntentRequest(
        val amount: Long,
        val currency: String,
        val customerId: String? = null,
        val paymentMethodTypes: List<String> = listOf("card")
    )

    private data class ConfirmPaymentIntentRequest(
        val paymentMethodId: String,
        val returnUrl: String? = null
    ) {
        constructor(params: ConfirmPaymentIntentParams) : this(
            paymentMethodId = params.paymentMethodId ?: "",
            returnUrl = params.returnUrl
        )
    }

    private data class CreateCustomerRequest(
        val email: String,
        val name: String? = null,
        val paymentMethodId: String? = null
    )

    private data class AttachPaymentMethodRequest(
        val customerId: String
    )

    private data class CreateSubscriptionRequest(
        val customerId: String,
        val priceId: String,
        val paymentMethodId: String? = null,
        val paymentBehavior: String = "default_incomplete"
    )

    private data class UpdateDefaultPaymentMethodRequest(
        val paymentMethodId: String
    )

    private data class StripeCustomer(
        val id: String,
        val email: String,
        val name: String?
    )

    private data class StripeSubscription(
        val id: String,
        val status: String,
        val customerId: String
    )

    private data class StripePaymentIntent(
        val id: String,
        val amount: Long,
        val currency: String,
        val clientSecret: String,
        val status: String,
        val created: Long,
        val livemode: Boolean,
        val paymentMethodTypes: List<String>,
        val countryCode: String?
    )
} 