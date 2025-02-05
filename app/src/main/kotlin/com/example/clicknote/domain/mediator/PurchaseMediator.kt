package com.example.clicknote.domain.mediator

import android.app.Activity
import com.example.clicknote.domain.model.Purchase
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

/**
 * Mediator interface for handling all purchase and subscription-related operations
 */
interface PurchaseMediator {
    /**
     * Observable state flows
     */
    val currentPlan: Flow<SubscriptionPlan>
    val subscriptionStatus: Flow<SubscriptionStatus>
    val purchases: Flow<Purchase>
    val purchaseHistory: Flow<List<String>>
    val activeSubscriptions: Flow<List<String>>

    /**
     * Billing connection management
     */
    suspend fun startBillingConnection()
    suspend fun endBillingConnection()
    suspend fun cleanup()

    /**
     * Subscription management
     */
    suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan)
    suspend fun purchaseSubscription(planId: String): Result<Unit>
    suspend fun cancelSubscription(): Result<Unit>
    suspend fun restorePurchases(): Result<Unit>
    
    /**
     * Purchase processing
     */
    suspend fun processPurchase(purchase: Purchase)
    suspend fun handlePurchaseError(error: Throwable)
    suspend fun acknowledgePurchase(purchaseToken: String)
    suspend fun consumePurchase(purchaseToken: String)

    /**
     * Weekly transcription limit management
     */
    suspend fun getWeeklyTranscriptionCount(): Int
    suspend fun incrementWeeklyTranscriptionCount()
    suspend fun resetWeeklyTranscriptionCount()
    suspend fun canMakeTranscription(): Boolean

    /**
     * Plan status checks
     */
    suspend fun isOnFreePlan(): Boolean
    suspend fun isOnPremiumPlan(): Boolean
    suspend fun isFeatureUnlocked(featureId: String): Boolean

    // Additional subscription-related methods
    suspend fun getPurchaseHistory(): Flow<List<String>>
    suspend fun getActiveSubscriptions(): Flow<List<String>>
    
    // Plan-specific methods
    suspend fun getRemainingFreeTranscriptions(): Int
    suspend fun checkTranscriptionLimit(): Boolean

    suspend fun initializeBilling()
    suspend fun querySubscriptions(): Flow<List<String>>
    suspend fun checkSubscriptionStatus(): Boolean
    suspend fun launchBillingFlow(productId: String)
} 