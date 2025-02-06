package com.example.clicknote.data.service

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.service.BillingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore,
    private val scope: CoroutineScope
) : BillingService {

    private var billingClient: BillingClient? = null
    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Free)
    override val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    init {
        scope.launch {
            initializeBillingClient()
        }
    }

    override suspend fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases -> 
                scope.launch {
                    handlePurchaseUpdate(billingResult, purchases)
                }
            }
            .enablePendingPurchases()
            .build()

        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryPurchases()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                scope.launch {
                    initializeBillingClient()
                }
            }
        })
    }

    override suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan) {
        val productId = when (plan) {
            is SubscriptionPlan.Monthly -> BillingService.MONTHLY_SUBSCRIPTION_ID
            is SubscriptionPlan.Annual -> BillingService.ANNUAL_SUBSCRIPTION_ID
            else -> return
        }

        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(productId)
                .setProductType(BillingClient.ProductType.SUBS)
                .build()
        )

        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()

        withContext(Dispatchers.IO) {
            billingClient?.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && productDetailsList.isNotEmpty()) {
                    val productDetails = productDetailsList[0]
                    val offerToken = productDetails.subscriptionOfferDetails?.get(0)?.offerToken
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
                        billingClient?.launchBillingFlow(activity, billingFlowParams)
                    }
                }
            }
        }
    }

    override suspend fun queryPurchases() {
        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        withContext(Dispatchers.IO) {
            billingClient?.queryPurchasesAsync(params) { billingResult, purchaseList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        handlePurchases(purchaseList)
                    }
                }
            }
        }
    }

    private suspend fun handlePurchases(purchases: List<Purchase>) {
        val validPurchase = purchases.firstOrNull { purchase ->
            purchase.purchaseState == Purchase.PurchaseState.PURCHASED
        }

        if (validPurchase != null) {
            when {
                validPurchase.products.contains(BillingService.MONTHLY_SUBSCRIPTION_ID) -> {
                    _subscriptionStatus.value = SubscriptionStatus.Premium(
                        expirationDate = validPurchase.purchaseTime + MONTHLY_DURATION,
                        isAutoRenewing = validPurchase.isAutoRenewing,
                        plan = SubscriptionPlan.Monthly()
                    )
                }
                validPurchase.products.contains(BillingService.ANNUAL_SUBSCRIPTION_ID) -> {
                    _subscriptionStatus.value = SubscriptionStatus.Premium(
                        expirationDate = validPurchase.purchaseTime + ANNUAL_DURATION,
                        isAutoRenewing = validPurchase.isAutoRenewing,
                        plan = SubscriptionPlan.Annual()
                    )
                }
                else -> _subscriptionStatus.value = SubscriptionStatus.Free
            }

            if (!validPurchase.isAcknowledged) {
                acknowledgePurchase(validPurchase.purchaseToken)
            }
        } else {
            _subscriptionStatus.value = SubscriptionStatus.Free
        }
    }

    private suspend fun acknowledgePurchase(purchaseToken: String) {
        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchaseToken)
            .build()
        billingClient?.acknowledgePurchase(params) { }
    }

    override fun endConnection() {
        billingClient?.endConnection()
        billingClient = null
    }

    override suspend fun canMakeTranscription(): Boolean {
        return when (val status = subscriptionStatus.value) {
            is SubscriptionStatus.Premium -> true
            is SubscriptionStatus.Free -> getRemainingTranscriptions() > 0
            else -> false
        }
    }

    override suspend fun getRemainingTranscriptions(): Int {
        val used = userPreferences.weeklyTranscriptionCount.first()
        return BillingService.FREE_WEEKLY_LIMIT - used
    }

    override suspend fun consumeTranscription() {
        if (subscriptionStatus.value !is SubscriptionStatus.Premium) {
            userPreferences.incrementWeeklyTranscriptionCount()
        }
    }

    override suspend fun resetTranscriptionCount() {
        userPreferences.resetWeeklyTranscriptionCount()
    }

    override suspend fun isPremium(): Boolean {
        return subscriptionStatus.value is SubscriptionStatus.Premium
    }

    private suspend fun handlePurchaseUpdate(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            handlePurchases(purchases)
        }
    }

    companion object {
        private const val MONTHLY_DURATION = 30L * 24 * 60 * 60 * 1000 // 30 days in milliseconds
        private const val ANNUAL_DURATION = 365L * 24 * 60 * 60 * 1000 // 365 days in milliseconds
    }
} 