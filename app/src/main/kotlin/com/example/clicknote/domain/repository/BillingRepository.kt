package com.example.clicknote.domain.repository

import android.app.Activity
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

data class SubscriptionDetails(
    val status: SubscriptionStatus,
    val expiryDate: Long? = null,
    val remainingFreeNotes: Int = 3,
    val isGracePeriod: Boolean = false,
    val gracePeriodEndDate: Long? = null
)

interface BillingRepository {
    val subscriptionStatus: Flow<SubscriptionStatus>
    val subscriptionDetails: Flow<SubscriptionDetails>
    val currentPlan: Flow<SubscriptionPlan>
    
    suspend fun startBillingConnection()
    suspend fun endBillingConnection()
    
    suspend fun purchaseMonthlySubscription()
    suspend fun purchaseAnnualSubscription()
    
    suspend fun acknowledgeSubscription(purchaseToken: String)
    suspend fun consumePurchase(purchaseToken: String)
    
    suspend fun restorePurchases(): Result<Unit>
    suspend fun querySubscriptions()
    
    suspend fun isSubscriptionActive(): Boolean
    suspend fun getRemainingFreeNotes(): Int
    suspend fun decrementFreeNotes()
    
    suspend fun handlePurchase(purchaseToken: String)
    suspend fun handleSubscriptionCanceled()
    suspend fun handleSubscriptionExpired()

    fun getSubscriptionPlans(): Flow<List<SubscriptionPlan>>
    suspend fun purchaseSubscription(planId: String): Result<Unit>
    suspend fun cancelSubscription(): Result<Unit>
    fun getCurrentPlan(): Flow<SubscriptionPlan?>

    suspend fun querySubscriptionPlans(): List<SubscriptionPlan>
    suspend fun purchaseSubscription(activity: Activity, plan: SubscriptionPlan)
    suspend fun acknowledgePurchase(purchaseToken: String)
    suspend fun getWeeklyTranscriptionCount(): Int
    suspend fun incrementWeeklyTranscriptionCount()
    suspend fun resetWeeklyTranscriptionCount()
    suspend fun checkWeeklyTranscriptionLimit(): Boolean
} 