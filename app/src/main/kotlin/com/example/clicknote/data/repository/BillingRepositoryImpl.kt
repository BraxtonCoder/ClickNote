package com.example.clicknote.data.repository

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.model.Purchase
import com.example.clicknote.domain.model.PurchaseStatus
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.BillingRepository
import com.example.clicknote.domain.repository.SubscriptionDetails
import com.example.clicknote.di.qualifiers.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataStore,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : BillingRepository {

    private var billingClient: BillingClient? = null
    private var currentActivity: Activity? = null

    private val _currentPlan = MutableStateFlow<SubscriptionPlan>(SubscriptionPlan.Free)
    override val currentPlan: Flow<SubscriptionPlan> = _currentPlan.asStateFlow()

    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Free)
    override val subscriptionStatus: Flow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    private val _subscriptionDetails = MutableStateFlow(
        SubscriptionDetails(
            status = SubscriptionStatus.Free,
            expiryDate = null,
            remainingFreeNotes = 3,
            isGracePeriod = false,
            gracePeriodEndDate = null
        )
    )
    override val subscriptionDetails: Flow<SubscriptionDetails> = _subscriptionDetails.asStateFlow()

    override suspend fun startBillingConnection() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases -> 
                coroutineScope.launch {
                    handlePurchaseUpdate(billingResult, purchases)
                }
            }
            .enablePendingPurchases()
            .build()
            
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    coroutineScope.launch {
                        querySubscriptions()
                        checkSubscriptionStatus()
                    }
                }
            }
            
            override fun onBillingServiceDisconnected() {
                coroutineScope.launch {
                    startBillingConnection()
                }
            }
        })
    }

    override suspend fun endBillingConnection() {
        billingClient?.endConnection()
        billingClient = null
    }

    override suspend fun purchaseMonthlySubscription() {
        currentActivity?.let { activity ->
            purchaseSubscription(activity, SubscriptionPlan.Monthly)
        }
    }

    override suspend fun purchaseAnnualSubscription() {
        currentActivity?.let { activity ->
            purchaseSubscription(activity, SubscriptionPlan.Annual)
        }
    }

    override suspend fun querySubscriptions() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SubscriptionPlan.Monthly.id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build(),
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(SubscriptionPlan.Annual.id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
            
        billingClient?.queryProductDetails(params)
    }

    override suspend fun isSubscriptionActive(): Boolean {
        return checkSubscriptionStatus()
    }

    override suspend fun getRemainingFreeNotes(): Int {
        val weeklyLimit = 3
        return weeklyLimit - getWeeklyTranscriptionCount()
    }

    override suspend fun decrementFreeNotes() {
        incrementWeeklyTranscriptionCount()
    }

    override fun getSubscriptionPlans(): Flow<List<SubscriptionPlan>> = flow {
        emit(listOf(SubscriptionPlan.Free, SubscriptionPlan.Monthly, SubscriptionPlan.Annual))
    }

    override fun getCurrentPlan(): Flow<SubscriptionPlan?> = currentPlan

    override suspend fun querySubscriptionPlans(): List<SubscriptionPlan> {
        return listOf(SubscriptionPlan.Free, SubscriptionPlan.Monthly, SubscriptionPlan.Annual)
    }

    override suspend fun purchaseSubscription(activity: Activity, plan: SubscriptionPlan) {
        currentActivity = activity
        when (plan) {
            is SubscriptionPlan.Monthly -> launchBillingFlow(activity, plan)
            is SubscriptionPlan.Annual -> launchBillingFlow(activity, plan)
            else -> {} // Free plan doesn't need billing flow
        }
    }

    override suspend fun purchaseSubscription(planId: String): Result<Unit> = runCatching {
        currentActivity?.let { activity ->
            when (planId) {
                SubscriptionPlan.Monthly.id -> purchaseSubscription(activity, SubscriptionPlan.Monthly)
                SubscriptionPlan.Annual.id -> purchaseSubscription(activity, SubscriptionPlan.Annual)
            }
        }
        Result.success(Unit)
    }

    override suspend fun cancelSubscription(): Result<Unit> = runCatching {
        handleSubscriptionCanceled()
        Result.success(Unit)
    }

    override suspend fun restorePurchases(): Result<Unit> = runCatching {
        checkSubscriptionStatus()
        Result.success(Unit)
    }

    override suspend fun acknowledgeSubscription(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient?.acknowledgePurchase(params)
    }

    override suspend fun consumePurchase(purchaseToken: String) {
        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient?.consumePurchase(params)
    }

    override suspend fun getWeeklyTranscriptionCount(): Int {
        return preferences.weeklyTranscriptionCount.first()
    }

    override suspend fun incrementWeeklyTranscriptionCount() {
        preferences.incrementWeeklyTranscriptionCount()
    }

    override suspend fun resetWeeklyTranscriptionCount() {
        preferences.resetWeeklyTranscriptionCount()
    }

    override suspend fun checkWeeklyTranscriptionLimit(): Boolean {
        return if (isSubscriptionActive()) {
            true
        } else {
            getRemainingFreeNotes() > 0
        }
    }

    override suspend fun handlePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient?.acknowledgePurchase(params)
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

    private suspend fun handlePurchaseUpdate(billingResult: BillingResult, purchases: List<com.android.billingclient.api.Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase.purchaseToken)
            }
        }
    }

    private suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(
                listOf(
                    QueryProductDetailsParams.Product.newBuilder()
                        .setProductId(plan.id)
                        .setProductType(BillingClient.ProductType.SUBS)
                        .build()
                )
            )
            .build()
            
        withContext(Dispatchers.IO) {
            val result = billingClient?.queryProductDetails(params)
            val productDetails = result?.productDetailsList?.firstOrNull() ?: return@withContext
            
            val billingFlowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(
                    listOf(
                        BillingFlowParams.ProductDetailsParams.newBuilder()
                            .setProductDetails(productDetails)
                            .build()
                    )
                )
                .build()
                
            billingClient?.launchBillingFlow(activity, billingFlowParams)
        }
    }

    private suspend fun checkSubscriptionStatus(): Boolean {
        return withContext(Dispatchers.IO) {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
                
            val result = billingClient?.queryPurchasesAsync(params)
            result?.purchasesList?.any { 
                it.purchaseState == com.android.billingclient.api.Purchase.PurchaseState.PURCHASED
            } ?: false
        }
    }

    private suspend fun updateSubscriptionStatus(status: SubscriptionStatus) {
        _subscriptionStatus.value = status
        preferences.setSubscriptionStatus(status)
        
        // Update subscription details
        _subscriptionDetails.value = when (status) {
            is SubscriptionStatus.Premium -> SubscriptionDetails(
                status = status,
                expiryDate = status.expirationDate,
                remainingFreeNotes = Int.MAX_VALUE,
                isGracePeriod = false,
                gracePeriodEndDate = null
            )
            else -> SubscriptionDetails(
                status = status,
                expiryDate = null,
                remainingFreeNotes = getRemainingFreeNotes(),
                isGracePeriod = false,
                gracePeriodEndDate = null
            )
        }
    }

    companion object {
        private const val SUBSCRIPTION_DURATION = 30L * 24 * 60 * 60 * 1000 // 30 days in milliseconds
    }
} 