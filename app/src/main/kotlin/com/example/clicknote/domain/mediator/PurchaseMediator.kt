package com.example.clicknote.domain.mediator

import android.app.Activity
import com.example.clicknote.domain.model.Purchase
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

interface PurchaseMediator {
    // Subscription state
    val currentPlan: Flow<SubscriptionPlan>
    val subscriptionStatus: Flow<SubscriptionStatus>
    val availablePlans: Flow<List<SubscriptionPlan>>

    // Billing operations
    suspend fun initialize()
    suspend fun startBillingConnection()
    suspend fun endBillingConnection()
    suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan)
    suspend fun processPurchase(purchase: Purchase)
    suspend fun handlePurchaseError(error: Throwable)
    suspend fun restorePurchases(): Result<Unit>

    // Subscription management
    suspend fun purchaseSubscription(planId: String): Result<Unit>
    suspend fun cancelSubscription(): Result<Unit>
    suspend fun acknowledgeSubscription(purchaseToken: String)
    suspend fun consumePurchase(purchaseToken: String)

    // Weekly transcription limit management
    suspend fun checkWeeklyTranscriptionLimit(): Boolean
    suspend fun getWeeklyTranscriptionCount(): Int
    suspend fun incrementWeeklyTranscriptionCount()
    suspend fun resetWeeklyTranscriptionCount()
    suspend fun getRemainingTranscriptions(): Int

    // Cleanup
    suspend fun cleanup()
} 