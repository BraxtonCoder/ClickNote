package com.example.clicknote.data.mediator

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.example.clicknote.domain.mediator.PurchaseMediator
import com.example.clicknote.domain.service.BillingService
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

private const val MONTHLY_SUBSCRIPTION_ID = "monthly_subscription"
private const val ANNUAL_SUBSCRIPTION_ID = "annual_subscription"
private const val FREE_WEEKLY_LIMIT = 3

@Singleton
class PurchaseMediatorImpl @Inject constructor(
    private val billingService: BillingService,
    private val userPreferences: UserPreferencesDataStore
) : PurchaseMediator {

    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _purchaseEvents = MutableSharedFlow<Purchase>()
    private var currentActivity: Activity? = null

    override suspend fun initializeBilling() {
        currentActivity?.let { activity ->
            billingService.initializeBillingClient(activity)
        }
    }

    override suspend fun querySubscriptions(): Flow<List<String>> = flow {
        val subscriptions = listOf(
            MONTHLY_SUBSCRIPTION_ID,  // £9.99/month
            ANNUAL_SUBSCRIPTION_ID    // £98/year
        )
        emit(subscriptions)
    }

    override suspend fun launchBillingFlow(productId: String) {
        val plan = when (productId) {
            MONTHLY_SUBSCRIPTION_ID -> SubscriptionPlan.MONTHLY
            ANNUAL_SUBSCRIPTION_ID -> SubscriptionPlan.ANNUAL
            else -> return
        }
        billingService.purchaseSubscription(plan)
    }

    override suspend fun checkSubscriptionStatus(): Boolean {
        val weeklyCount = getWeeklyTranscriptionCount()
        return when {
            billingService.currentPlan.value == SubscriptionPlan.FREE && weeklyCount >= FREE_WEEKLY_LIMIT -> false
            billingService.currentPlan.value in listOf(SubscriptionPlan.MONTHLY, SubscriptionPlan.ANNUAL) -> true
            else -> weeklyCount < FREE_WEEKLY_LIMIT
        }
    }

    override suspend fun endBillingConnection() {
        billingService.endBillingConnection()
        currentActivity = null
    }

    override fun getWeeklyTranscriptionCount(): Int {
        return userPreferences.getWeeklyTranscriptionCount()
    }

    override fun incrementWeeklyTranscriptionCount() {
        userPreferences.incrementWeeklyTranscriptionCount()
    }

    override fun resetWeeklyTranscriptionCount() {
        userPreferences.resetWeeklyTranscriptionCount()
    }

    fun setActivity(activity: Activity) {
        currentActivity = activity
        initializeBillingScope()
    }

    private fun initializeBillingScope() {
        currentActivity?.let { activity ->
            coroutineScope.launch {
                billingService.initializeBillingClient(activity)
            }
        }
    }

    init {
        billingService.setPurchaseCallback { purchase ->
            coroutineScope.launch {
                _purchaseEvents.emit(purchase)
            }
        }
    }

    suspend fun emitPurchase(purchase: Purchase) {
        _purchaseEvents.emit(purchase)
    }
} 