package com.example.clicknote.data.service

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.service.BillingService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val scope: CoroutineScope
) : BillingService, PurchasesUpdatedListener {

    private lateinit var billingClient: BillingClient
    private val _subscriptionStatus = MutableStateFlow<SubscriptionPlan>(SubscriptionPlan.Free)
    override val subscriptionStatus: StateFlow<SubscriptionPlan> = _subscriptionStatus

    init {
        scope.launch {
            initializeBillingClient()
        }
    }

    override suspend fun initializeBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener(this)
            .enablePendingPurchases()
            .build()

        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        queryPurchases()
                    }
                }
            }

            override fun onBillingServiceDisconnected() {
                // Try to restart the connection on the next request
                scope.launch {
                    initializeBillingClient()
                }
            }
        })
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: List<Purchase>?
    ) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            scope.launch {
                for (purchase in purchases) {
                    handlePurchase(purchase)
                }
            }
        }
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            when {
                purchase.products.contains(MONTHLY_SUB_ID) -> _subscriptionStatus.value = SubscriptionPlan.Monthly
                purchase.products.contains(ANNUAL_SUB_ID) -> _subscriptionStatus.value = SubscriptionPlan.Annual
            }

            if (!purchase.isAcknowledged) {
                val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                    .setPurchaseToken(purchase.purchaseToken)
                    .build()
                billingClient.acknowledgePurchase(acknowledgePurchaseParams) { }
            }
        }
    }

    override suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan) {
        val productId = when (plan) {
            SubscriptionPlan.Monthly -> MONTHLY_SUB_ID
            SubscriptionPlan.Annual -> ANNUAL_SUB_ID
            SubscriptionPlan.Free -> return
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
            billingClient.queryProductDetailsAsync(params) { billingResult, productDetailsList ->
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
                        billingClient.launchBillingFlow(activity, billingFlowParams)
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
            billingClient.queryPurchasesAsync(params) { billingResult, purchaseList ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    scope.launch {
                        for (purchase in purchaseList) {
                            handlePurchase(purchase)
                        }
                    }
                }
            }
        }
    }

    override fun endConnection() {
        billingClient.endConnection()
    }

    companion object {
        private const val MONTHLY_SUB_ID = "clicknote_monthly_subscription"
        private const val ANNUAL_SUB_ID = "clicknote_annual_subscription"
    }
} 