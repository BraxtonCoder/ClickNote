package com.example.clicknote.data.mediator

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.mediator.PurchaseMediator
import com.example.clicknote.domain.model.Purchase
import com.example.clicknote.domain.model.PurchaseStatus
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseMediatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataStore,
    private val coroutineScope: CoroutineScope
) : PurchaseMediator {
    
    private var billingClient: BillingClient? = null
    private var currentActivity: Activity? = null
    
    private val _currentPlan = MutableStateFlow<SubscriptionPlan>(SubscriptionPlan.Free)
    override val currentPlan: Flow<SubscriptionPlan> = _currentPlan.asStateFlow()
    
    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Free)
    override val subscriptionStatus: Flow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    private val _purchases = MutableStateFlow<Purchase?>(null)
    override val purchases: Flow<Purchase> = _purchases.filterNotNull()

    override suspend fun initializeBilling() {
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
                    initializeBilling()
                }
            }
        })
    }
    
    override suspend fun querySubscriptions(): Flow<List<String>> = flow {
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
            
        withContext(Dispatchers.IO) {
            val result = billingClient?.queryProductDetails(params)
            emit(result?.productDetailsList?.map { it.productId } ?: emptyList())
        }
    }
    
    override suspend fun launchBillingFlow(productId: String) {
        val activity = currentActivity ?: return
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
    
    override suspend fun checkSubscriptionStatus(): Boolean {
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

    override suspend fun getPurchaseHistory(): Flow<List<String>> = flow {
        withContext(Dispatchers.IO) {
            val params = QueryPurchaseHistoryParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
                
            val result = billingClient?.queryPurchaseHistory(params)
            emit(result?.purchaseHistoryRecordList?.map { it.products.first() } ?: emptyList())
        }
    }

    override suspend fun getActiveSubscriptions(): Flow<List<String>> = flow {
        withContext(Dispatchers.IO) {
            val params = QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
                
            val result = billingClient?.queryPurchasesAsync(params)
            emit(result?.purchasesList?.map { it.products.first() } ?: emptyList())
        }
    }

    private suspend fun handlePurchaseUpdate(billingResult: BillingResult, purchases: List<com.android.billingclient.api.Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                processPurchase(
                    Purchase(
                        id = purchase.orderId ?: "",
                        userId = "",  // Get from your auth system
                        plan = _currentPlan.value,
                        amount = 0.0, // Get from product details
                        status = PurchaseStatus.COMPLETED
                    )
                )
            }
        }
    }

    override suspend fun endBillingConnection() {
        billingClient?.endConnection()
        billingClient = null
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

    override suspend fun isOnFreePlan(): Boolean {
        return !isOnPremiumPlan()
    }

    override suspend fun isOnPremiumPlan(): Boolean {
        return checkSubscriptionStatus()
    }

    override suspend fun getRemainingFreeTranscriptions(): Int {
        val weeklyLimit = 3
        return weeklyLimit - getWeeklyTranscriptionCount()
    }

    override suspend fun checkTranscriptionLimit(): Boolean {
        return if (isOnPremiumPlan()) {
            true
        } else {
            getRemainingFreeTranscriptions() > 0
        }
    }

    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }

    override suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan) {
        currentActivity = activity
        when (plan) {
            is SubscriptionPlan.Monthly -> launchBillingFlow("monthly_subscription")
            is SubscriptionPlan.Annual -> launchBillingFlow("annual_subscription")
            else -> {} // Free plan doesn't need billing flow
        }
    }

    override suspend fun purchaseSubscription(planId: String): Result<Unit> = runCatching {
        launchBillingFlow(planId)
        Result.success(Unit)
    }

    override suspend fun cancelSubscription(): Result<Unit> = runCatching {
        // Implementation for canceling subscription
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

    override suspend fun canMakeTranscription(): Boolean {
        return isOnPremiumPlan() || getRemainingFreeTranscriptions() > 0
    }

    override suspend fun processPurchase(purchase: Purchase) {
        _purchases.value = purchase
    }

    override suspend fun handlePurchaseError(error: Throwable) {
        // Implementation for handling purchase error
    }

    override suspend fun cleanup() {
        endBillingConnection()
    }
}