package com.example.clicknote.service.impl

import android.app.Activity
import com.android.billingclient.api.*
import com.example.clicknote.domain.model.SubscriptionState
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.repository.SubscriptionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class BillingServiceImpl @Inject constructor(
    private val userPreferences: Lazy<UserPreferencesDataStore>,
    private val stateController: Lazy<SubscriptionStateController>,
    private val repository: SubscriptionRepository
) : BillingService {

    private var billingClient: Any? = null
    private var currentActivity: Activity? = null

    override val subscriptionState: StateFlow<SubscriptionState> = stateController.get().subscriptionState
    override val isPremium: StateFlow<Boolean> = repository.isPremium

    override fun initializeBillingClient(activity: Activity) {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(this::handlePurchase)
            .enablePendingPurchases()
            .build()
    }

    private fun handlePurchase(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && !purchases.isNullOrEmpty()) {
            val purchase = purchases[0]
            when (purchase.purchaseState) {
                Purchase.PurchaseState.PURCHASED -> {
                    when (purchase.products[0]) {
                        MONTHLY_SUB_PRODUCT_ID -> repository.updateSubscriptionState(SubscriptionState.MONTHLY)
                        ANNUAL_SUB_PRODUCT_ID -> repository.updateSubscriptionState(SubscriptionState.ANNUAL)
                    }
                }
                Purchase.PurchaseState.PENDING -> repository.updateSubscriptionState(SubscriptionState.PENDING)
                else -> repository.updateSubscriptionState(SubscriptionState.FREE)
            }
        }
    }

    override suspend fun checkTranscriptionAvailability(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val status = stateController.get().subscriptionState.value
            val count = userPreferences.get().getWeeklyTranscriptionCount()
            
            val canTranscribe = when (status) {
                SubscriptionState.MONTHLY, SubscriptionState.ANNUAL -> true
                SubscriptionState.FREE -> count > 0
                else -> false
            }
            
            Result.success(canTranscribe)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun purchaseMonthlySubscription() {
        stateController.get().updateSubscriptionState(SubscriptionState.MONTHLY)
    }

    override suspend fun purchaseAnnualSubscription() {
        stateController.get().updateSubscriptionState(SubscriptionState.ANNUAL)
    }

    override suspend fun restorePurchases() {
        querySubscriptions()
    }

    override suspend fun hasActiveSubscription(): Boolean {
        return stateController.get().isPremium.value
    }

    override suspend fun endBillingConnection() {
        stateController.get().resetSubscriptionState()
        billingClient?.endConnection()
        billingClient = null
    }

    override fun isPremiumUser(): Boolean = stateController.get().isPremium.value

    override suspend fun getRemainingFreeRecordings(): Int {
        return userPreferences.get().getWeeklyTranscriptionCount()
    }

    override suspend fun consumeFreeRecording() {
        stateController.get().consumeFreeRecording()
    }

    override fun resetFreeRecordingsCount() {
        stateController.get().resetFreeRecordingsCount()
    }

    fun setActivity(activity: Activity?) {
        currentActivity = activity
    }

    companion object {
        private const val MONTHLY_SUB_PRODUCT_ID = "monthly_subscription"
        private const val ANNUAL_SUB_PRODUCT_ID = "annual_subscription"
    }
} 