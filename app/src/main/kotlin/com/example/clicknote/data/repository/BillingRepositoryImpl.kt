package com.example.clicknote.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.repository.BillingRepository
import com.example.clicknote.domain.repository.SubscriptionDetails
import com.example.clicknote.domain.repository.SubscriptionStatus
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    private val coroutineScope: CoroutineScope
) : BillingRepository {

    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus.FREE)
    override val subscriptionStatus = _subscriptionStatus.asStateFlow()

    private val _subscriptionDetails = MutableStateFlow(SubscriptionDetails(SubscriptionStatus.FREE))
    override val subscriptionDetails = _subscriptionDetails.asStateFlow()

    private var billingClient: BillingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    private var currentPurchase: Purchase? = null

    private val monthlySkuDetails = MutableStateFlow<ProductDetails?>(null)
    private val annualSkuDetails = MutableStateFlow<ProductDetails?>(null)

    private val _subscriptionPlans = MutableStateFlow<List<ProductDetails>>(emptyList())
    private val _currentPlan = MutableStateFlow<SubscriptionPlan?>(null)

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            coroutineScope.launch(Dispatchers.IO) {
                for (purchase in purchases) {
                    handlePurchase(purchase.purchaseToken)
                }
            }
        }
    }

    init {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    coroutineScope.launch {
                        querySubscriptions()
                        restorePurchases()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection
                billingClient.startConnection(this)
            }
        })
    }

    override suspend fun querySubscriptions() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("monthly_subscription")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId("annual_subscription")
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        val result = billingClient.queryProductDetails(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            _subscriptionPlans.value = result.productDetailsList ?: emptyList()
        }
    }

    override suspend fun purchaseSubscription(planId: String): Result<Unit> = runCatching {
        val productDetails = _subscriptionPlans.value.find { it.productId == planId }
            ?: throw IllegalArgumentException("Invalid plan ID")

        val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            ?: throw IllegalStateException("No offer token available")

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(
                listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(productDetails)
                        .setOfferToken(offerToken)
                        .build()
                )
            )
            .build()

        // Launch billing flow
        val activity = context as? Activity
            ?: throw IllegalStateException("Context is not an activity")
        
        val responseCode = billingClient.launchBillingFlow(activity, billingFlowParams).responseCode
        if (responseCode != BillingClient.BillingResponseCode.OK) {
            throw IllegalStateException("Failed to launch billing flow")
        }
    }

    override suspend fun cancelSubscription(): Result<Unit> = runCatching {
        // Implementation depends on your backend service
        // You'll need to call your server to cancel the subscription
    }

    override suspend fun acknowledgeSubscription(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.acknowledgePurchase(params)
    }

    override suspend fun consumePurchase(purchaseToken: String) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient.consumePurchase(params)
    }

    override suspend fun restorePurchases(): Result<Unit> = runCatching {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        
        val result = billingClient.queryPurchases(params)
        if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            result.purchasesList.forEach { purchase ->
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    coroutineScope.launch {
                        handlePurchase(purchase.purchaseToken)
                    }
                }
            }
        }
    }

    override suspend fun handlePurchase(purchaseToken: String) {
        // Verify purchase with backend
        // Update subscription status
        updateSubscriptionStatus(SubscriptionStatus.MONTHLY) // or ANNUAL based on purchase
    }

    override suspend fun handleSubscriptionCanceled() {
        updateSubscriptionStatus(SubscriptionStatus.GRACE_PERIOD)
    }

    override suspend fun handleSubscriptionExpired() {
        updateSubscriptionStatus(SubscriptionStatus.EXPIRED)
    }

    private fun updateSubscriptionStatus(status: SubscriptionStatus) {
        _subscriptionStatus.value = status
        _subscriptionDetails.value = when (status) {
            SubscriptionStatus.FREE -> SubscriptionDetails(
                status = status,
                remainingFreeNotes = 3
            )
            SubscriptionStatus.GRACE_PERIOD -> SubscriptionDetails(
                status = status,
                isGracePeriod = true,
                gracePeriodEndDate = System.currentTimeMillis() + GRACE_PERIOD_DURATION
            )
            else -> SubscriptionDetails(
                status = status,
                expiryDate = System.currentTimeMillis() + SUBSCRIPTION_DURATION
            )
        }
    }

    override suspend fun isSubscriptionActive(): Boolean {
        return _subscriptionStatus.value != SubscriptionStatus.FREE &&
                _subscriptionStatus.value != SubscriptionStatus.EXPIRED
    }

    override suspend fun getRemainingFreeNotes(): Int {
        return _subscriptionDetails.value.remainingFreeNotes
    }

    override suspend fun decrementFreeNotes() {
        _subscriptionDetails.value = _subscriptionDetails.value.copy(
            remainingFreeNotes = (_subscriptionDetails.value.remainingFreeNotes - 1).coerceAtLeast(0)
        )
    }

    override fun getSubscriptionPlans(): Flow<List<SubscriptionPlan>> =
        _subscriptionPlans.map { productDetails ->
            productDetails.map { details ->
                SubscriptionPlan(
                    id = details.productId,
                    name = details.name,
                    description = details.description,
                    price = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases
                        ?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "",
                    period = when (details.productId) {
                        "monthly_subscription" -> "Monthly"
                        "annual_subscription" -> "Annual"
                        else -> "Unknown"
                    }
                )
            }
        }

    override fun getCurrentPlan(): Flow<SubscriptionPlan?> = _currentPlan

    companion object {
        private const val GRACE_PERIOD_DURATION = 7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds
        private const val SUBSCRIPTION_DURATION = 30 * 24 * 60 * 60 * 1000L // 30 days in milliseconds
    }
} 