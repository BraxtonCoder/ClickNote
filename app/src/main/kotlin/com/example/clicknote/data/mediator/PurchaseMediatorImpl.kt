package com.example.clicknote.data.mediator

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.mediator.PurchaseMediator
import com.example.clicknote.domain.model.Purchase
import com.example.clicknote.domain.model.PurchaseStatus
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PurchaseMediatorImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferences: UserPreferencesDataStore
) : PurchaseMediator {
    
    private var billingClient: BillingClient? = null
    private var currentActivity: Activity? = null
    
    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.FREE)
    override val subscriptionStatus: Flow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    override suspend fun initializeBilling() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases -> 
                // Handle purchase updates
            }
            .enablePendingPurchases()
            .build()
            
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    // Billing client is ready
                }
            }
            
            override fun onBillingServiceDisconnected() {
                // Try to restart the connection
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
            
        val result = billingClient?.queryProductDetails(params)
        emit(result?.productDetailsList?.map { it.productId } ?: emptyList())
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
            
        val result = billingClient?.queryProductDetails(params)
        val productDetails = result?.productDetailsList?.firstOrNull() ?: return
        
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
    
    override suspend fun checkSubscriptionStatus(): Boolean {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
            
        val result = billingClient?.queryPurchasesAsync(params)
        return result?.purchasesList?.any { 
            it.purchaseState == Purchase.PurchaseState.PURCHASED 
        } ?: false
    }
    
    override suspend fun endBillingConnection() {
        billingClient?.endConnection()
        billingClient = null
    }
    
    override fun getWeeklyTranscriptionCount(): Int {
        return preferences.getWeeklyTranscriptionCount()
    }
    
    override fun incrementWeeklyTranscriptionCount() {
        preferences.incrementWeeklyTranscriptionCount()
    }
    
    override fun resetWeeklyTranscriptionCount() {
        preferences.resetWeeklyTranscriptionCount()
    }
    
    override suspend fun getPurchaseHistory(): Flow<List<String>> = flow {
        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
            
        val result = billingClient?.queryPurchaseHistory(params)
        emit(result?.purchaseHistoryRecordList?.map { it.products.first() } ?: emptyList())
    }
    
    override suspend fun acknowledgePurchase(purchaseToken: String) {
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
    
    override suspend fun isFeatureUnlocked(featureId: String): Boolean {
        return when (featureId) {
            "unlimited_transcriptions" -> isOnPremiumPlan()
            else -> false
        }
    }
    
    override suspend fun getActiveSubscriptions(): Flow<List<String>> = flow {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
            
        val result = billingClient?.queryPurchasesAsync(params)
        emit(result?.purchasesList?.map { it.products.first() } ?: emptyList())
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
}