package com.example.clicknote.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionPeriod
import com.example.clicknote.domain.model.SubscriptionType
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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.example.clicknote.domain.repository.UserPreferencesDataStore
import com.example.clicknote.di.ApplicationScope

private const val GRACE_PERIOD_DURATION = 7L * 24 * 60 * 60 * 1000 // 7 days in milliseconds
private const val SUBSCRIPTION_DURATION = 30L * 24 * 60 * 60 * 1000 // 30 days in milliseconds

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val auth: FirebaseAuth,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val billingClient: BillingClient,
    private val userPreferences: UserPreferencesDataStore
) : BillingRepository {

    private val _subscriptionStatus = MutableStateFlow(SubscriptionStatus.Free)
    override val subscriptionStatus: Flow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    private val _subscriptionDetails = MutableStateFlow(SubscriptionDetails(SubscriptionStatus.Free))
    override val subscriptionDetails: Flow<SubscriptionDetails> = _subscriptionDetails.asStateFlow()

    private val _currentPlan = MutableStateFlow<SubscriptionPlan>(SubscriptionPlan.FREE)
    override fun getCurrentPlan(): Flow<SubscriptionPlan?> = _currentPlan.asStateFlow()

    private val _subscriptionPlans = MutableStateFlow<List<ProductDetails>>(emptyList())

    private var currentPurchase: Purchase? = null

    private val monthlySkuDetails = MutableStateFlow<ProductDetails?>(null)
    private val annualSkuDetails = MutableStateFlow<ProductDetails?>(null)

    private val _availablePlans = MutableStateFlow<List<SubscriptionPlan>>(emptyList())
    override val availablePlans: Flow<List<SubscriptionPlan>> = _availablePlans.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            coroutineScope.launch(Dispatchers.IO) {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }
    }

    init {
        startBillingConnection()
    }

    override suspend fun startBillingConnection() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    coroutineScope.launch {
                        querySubscriptions()
                        checkSubscriptionStatus()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                startBillingConnection()
            }
        })
    }

    override suspend fun endBillingConnection() {
        billingClient.endConnection()
    }

    override suspend fun querySubscriptions() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SubscriptionPlan.MONTHLY.productId)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SubscriptionPlan.ANNUAL.productId)
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

    override suspend fun purchaseMonthlySubscription() {
        purchaseSubscription(SubscriptionPlan.MONTHLY.productId)
    }

    override suspend fun purchaseAnnualSubscription() {
        purchaseSubscription(SubscriptionPlan.ANNUAL.productId)
    }

    override suspend fun purchaseSubscription(planId: String): Result<Unit> {
        val productDetails = _subscriptionPlans.value.find { it.productId == planId }
        return if (productDetails != null) {
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
                // Note: This would need to be called from an Activity context
                Result.success(Unit)
            } else {
                Result.failure(Exception("No offer token available for subscription"))
            }
        } else {
            Result.failure(Exception("Product details not found for plan: $planId"))
        }
    }

    override suspend fun handlePurchase(purchaseToken: String) {
        acknowledgeSubscription(purchaseToken)
        updateSubscriptionStatus(SubscriptionStatus.Premium(
            expirationDate = System.currentTimeMillis() + SUBSCRIPTION_DURATION,
            isAutoRenewing = true,
            plan = _currentPlan.value
        ))
    }

    override suspend fun handleSubscriptionCanceled() {
        updateSubscriptionStatus(SubscriptionStatus.Free)
    }

    override suspend fun handleSubscriptionExpired() {
        updateSubscriptionStatus(SubscriptionStatus.Free)
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

    override suspend fun restorePurchases(): Result<Unit> {
        return try {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
            val result = billingClient.queryPurchasesAsync(params)
            if (result.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val activePurchase = result.purchasesList.firstOrNull { 
                    it.purchaseState == Purchase.PurchaseState.PURCHASED 
                }
                if (activePurchase != null) {
                    handlePurchase(activePurchase.purchaseToken)
                } else {
                    handleSubscriptionExpired()
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelSubscription(): Result<Unit> {
        return try {
            handleSubscriptionCanceled()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun isSubscriptionActive(): Boolean {
        return when (val status = _subscriptionStatus.value) {
            is SubscriptionStatus.Premium -> true
            else -> false
        }
    }

    override suspend fun getRemainingFreeNotes(): Int {
        return when (val status = _subscriptionStatus.value) {
            is SubscriptionStatus.Free -> SubscriptionPlan.FREE.weeklyTranscriptionLimit
            else -> Int.MAX_VALUE
        }
    }

    override suspend fun decrementFreeNotes() {
        // This should be handled by UserPreferencesDataStore
        userPreferences.incrementWeeklyTranscriptionCount()
    }

    override fun getSubscriptionPlans(): Flow<List<SubscriptionPlan>> = 
        _subscriptionPlans.map { productDetails ->
            productDetails.map { details ->
                SubscriptionPlan(
                    id = details.productId,
                    name = details.title,
                    description = details.description,
                    price = details.subscriptionOfferDetails?.firstOrNull()?.pricingPhases
                        ?.pricingPhaseList?.firstOrNull()?.formattedPrice ?: "",
                    period = when {
                        details.productId.contains("monthly") -> SubscriptionPeriod.MONTHLY
                        details.productId.contains("annual") -> SubscriptionPeriod.ANNUAL
                        else -> SubscriptionPeriod.NONE
                    },
                    productId = details.productId
                )
            }
        }

    private fun updateSubscriptionStatus(status: SubscriptionStatus) {
        _subscriptionStatus.value = status
        when (status) {
            is SubscriptionStatus.Free -> {
                _currentPlan.value = SubscriptionPlan.FREE
            }
            is SubscriptionStatus.Premium -> {
                _currentPlan.value = when (status.plan.period) {
                    SubscriptionPeriod.MONTHLY -> SubscriptionPlan.MONTHLY
                    SubscriptionPeriod.ANNUAL -> SubscriptionPlan.ANNUAL
                    else -> SubscriptionPlan.FREE
                }
            }
            else -> { /* no-op */ }
        }
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
                    updateSubscriptionStatus(SubscriptionStatus.Free)
                }
            }
        }
    }

    private fun verifySubscription(purchaseToken: String) {
        // Here you would typically verify the purchase with your backend
        // For now, we'll just assume it's valid
        updateSubscriptionStatus(SubscriptionStatus.Premium(
            expirationDate = System.currentTimeMillis() + SUBSCRIPTION_DURATION,
            isAutoRenewing = true,
            plan = _currentPlan.value
        ))
    }

    override suspend fun checkWeeklyTranscriptionLimit(): Boolean {
        return when (_subscriptionStatus.value) {
            SubscriptionStatus.Free -> {
                val count = userPreferences.weeklyTranscriptionCount.first()
                count < FREE_WEEKLY_LIMIT
            }
            SubscriptionStatus.Premium -> true
            else -> false
        }
    }

    override suspend fun getWeeklyTranscriptionCount(): Int {
        return userPreferences.getWeeklyTranscriptionCount()
    }

    override suspend fun incrementWeeklyTranscriptionCount() {
        userPreferences.incrementWeeklyTranscriptionCount()
    }

    override suspend fun resetWeeklyTranscriptionCount() {
        userPreferences.resetWeeklyTranscriptionCount()
    }

    companion object {
        private const val FREE_WEEKLY_LIMIT = 3
    }
} 