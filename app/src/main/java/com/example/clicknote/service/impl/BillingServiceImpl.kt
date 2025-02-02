package com.example.clicknote.service.impl

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.service.BillingService
import com.example.clicknote.domain.repository.SubscriptionRepository
import com.example.clicknote.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val subscriptionRepository: SubscriptionRepository
) : BillingService {

    private var billingClient: BillingClient? = null
    private var currentActivity: Activity? = null

    override fun initializeBillingClient(activity: Activity) {
        currentActivity = activity
        setupBillingClient()
    }

    override fun endBillingConnection() {
        billingClient?.endConnection()
        billingClient = null
    }

    override suspend fun purchaseSubscription(plan: SubscriptionPlan) {
        val productId = when (plan) {
            SubscriptionPlan.MONTHLY -> MONTHLY_SUBSCRIPTION_ID
            SubscriptionPlan.ANNUAL -> ANNUAL_SUBSCRIPTION_ID
            else -> return
        }
        launchBillingFlow(productId)
    }

    private fun setupBillingClient() {
        billingClient = BillingClient.newBuilder(context)
            .setListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    coroutineScope.launch {
                        for (purchase in purchases) {
                            handlePurchase(purchase)
                        }
                    }
                }
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
                // Try to restart the connection on the next request to
                // Google Play by calling the startConnection() method.
            }
        })
    }

    private suspend fun handlePurchase(purchase: Purchase) {
        when (purchase.products.firstOrNull()) {
            MONTHLY_SUBSCRIPTION_ID -> subscriptionRepository.updateSubscriptionState(SubscriptionPlan.MONTHLY)
            ANNUAL_SUBSCRIPTION_ID -> subscriptionRepository.updateSubscriptionState(SubscriptionPlan.ANNUAL)
        }
    }

    private suspend fun launchBillingFlow(productId: String) {
        val activity = currentActivity ?: return
        val client = billingClient ?: return

        try {
            val productList = listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.SUBS)
                    .build()
            )

            val params = QueryProductDetailsParams.newBuilder()
                .setProductList(productList)
                .build()

            val productDetailsResult = client.queryProductDetails(params)
            
            if (productDetailsResult.billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val productDetails = productDetailsResult.productDetailsList?.firstOrNull()
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
                        client.launchBillingFlow(activity, billingFlowParams)
                    }
                }
            }
        } catch (e: Exception) {
            // Handle any errors
        }
    }

    companion object {
        private const val MONTHLY_SUBSCRIPTION_ID = "monthly_subscription"
        private const val ANNUAL_SUBSCRIPTION_ID = "annual_subscription"
    }
} 