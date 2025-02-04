package com.example.clicknote.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
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
import com.example.clicknote.domain.repository.UserPreferencesDataStore
import com.example.clicknote.di.ApplicationScope

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val billingClient: BillingClient,
    private val userPreferences: UserPreferencesDataStore
) : BillingRepository {

    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.NONE)
    override val subscriptionStatus: Flow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    private val _subscriptionDetails = MutableStateFlow(SubscriptionDetails(SubscriptionStatus.FREE))
    override val subscriptionDetails = _subscriptionDetails.asStateFlow()

    private var currentPurchase: Purchase? = null

    private val monthlySkuDetails = MutableStateFlow<ProductDetails?>(null)
    private val annualSkuDetails = MutableStateFlow<ProductDetails?>(null)

    private val _subscriptionPlans = MutableStateFlow<List<ProductDetails>>(emptyList())
    private val _currentPlan = MutableStateFlow<SubscriptionPlan?>(null)

    private val _availablePlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    override val availablePlans: Flow<List<SubscriptionPlan>> = _availablePlans.asStateFlow()

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
        setupBillingClient()
    }

    private fun setupBillingClient() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryAvailableSubscriptions()
                    checkSubscriptionStatus()
                }
            }

            override fun onBillingServiceDisconnected() {
                // Retry connection
                setupBillingClient()
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

    override suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan) {
        val productDetails = getProductDetails(plan.id)
        if (productDetails != null) {
            val offerToken = productDetails.subscriptionOfferDetails?.firstOrNull()?.offerToken
            if (offerToken != null) {
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
                billingClient.launchBillingFlow(activity, billingFlowParams)
            }
        }
    }

    private suspend fun getProductDetails(productId: String): ProductDetails? {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()

        return billingClient.queryProductDetails(params).productDetailsList?.firstOrNull()
    }

    private fun queryAvailableSubscriptions() {
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

        billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                _availablePlans.value = productDetailsList.map { details ->
                    SubscriptionPlan(
                        id = details.productId,
                        name = details.title,
                        description = details.description,
                        price = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "",
                        period = when {
                            details.productId.contains("monthly") -> SubscriptionPeriod.MONTHLY
                            details.productId.contains("annual") -> SubscriptionPeriod.ANNUAL
                            else -> SubscriptionPeriod.MONTHLY
                        },
                        type = SubscriptionType.PREMIUM
                    )
                }
            }
        }
    }

    private fun checkSubscriptionStatus() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        ) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val activePurchase = purchases.firstOrNull { purchase ->
                    purchase.purchaseState == Purchase.PurchaseState.PURCHASED
                }

                if (activePurchase != null) {
                    verifySubscription(activePurchase.purchaseToken)
                } else {
                    updateSubscriptionStatus(SubscriptionStatus.FREE)
                }
            }
        }
    }

    private fun verifySubscription(purchaseToken: String) {
        // Here you would typically verify the purchase with your backend
        // For now, we'll just assume it's valid
        updateSubscriptionStatus(SubscriptionStatus.MONTHLY)
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

    override suspend fun checkWeeklyTranscriptionLimit(): Boolean {
        return when (_subscriptionStatus.value) {
            SubscriptionStatus.FREE -> {
                val count = userPreferences.weeklyTranscriptionCount.first()
                count < FREE_WEEKLY_LIMIT
            }
            SubscriptionStatus.MONTHLY, SubscriptionStatus.ANNUAL -> true
            else -> false
        }
    }

    companion object {
        private const val GRACE_PERIOD_DURATION = 7 * 24 * 60 * 60 * 1000L // 7 days in milliseconds
        private const val SUBSCRIPTION_DURATION = 30 * 24 * 60 * 60 * 1000L // 30 days in milliseconds
        private const val FREE_WEEKLY_LIMIT = 3
    }
} 