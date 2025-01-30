package com.example.clicknote.service

import android.app.Activity
import com.example.clicknote.domain.model.SubscriptionState
import kotlinx.coroutines.flow.StateFlow

interface BillingService {
    /**
     * Current subscription state
     */
    val subscriptionState: StateFlow<SubscriptionState>

    /**
     * Whether user has premium subscription
     */
    val isPremium: StateFlow<Boolean>

    /**
     * Available subscription plans
     */
    val subscriptionPlans: Flow<List<SubscriptionPlan>>

    /**
     * Whether user can transcribe based on subscription status and usage
     */
    val canTranscribe: Flow<Boolean>

    /**
     * Number of remaining transcriptions for free users
     */
    val remainingTranscriptions: Flow<Int>

    /**
     * Number of remaining free recordings for free users
     */
    val freeRecordingsRemaining: Flow<Int>

    /**
     * Purchase monthly subscription
     */
    suspend fun purchaseMonthlySubscription(): Result<Unit>

    /**
     * Purchase annual subscription
     */
    suspend fun purchaseAnnualSubscription(): Result<Unit>

    /**
     * Restore purchases
     */
    suspend fun restorePurchases(): Result<Unit>

    /**
     * Check if user has active subscription
     */
    fun hasActiveSubscription(): Boolean

    /**
     * Check if user can transcribe based on subscription status and usage
     */
    suspend fun checkTranscriptionAvailability(): Result<Boolean>

    /**
     * Increment transcription count for free users
     */
    suspend fun incrementTranscriptionCount(): Result<Unit>

    /**
     * Reset transcription count for free users
     */
    suspend fun resetTranscriptionCount(): Result<Unit>

    /**
     * Show subscription options to the user
     */
    suspend fun showSubscriptionOptions()

    /**
     * Manage existing subscription
     */
    suspend fun manageSubscription()

    /**
     * Initialize billing connection
     */
    fun initializeBillingClient(activity: Activity)

    /**
     * End billing connection
     */
    fun endBillingConnection()

    /**
     * Check if user is premium
     */
    fun isPremiumUser(): Boolean

    /**
     * Get remaining free recordings for free users
     */
    fun getRemainingFreeRecordings(): Int

    /**
     * Consume a free recording
     */
    fun consumeFreeRecording()

    /**
     * Reset free recordings count
     */
    fun resetFreeRecordingsCount()

    /**
     * Open premium purchase
     */
    suspend fun openPremiumPurchase()

    /**
     * Open manage subscriptions
     */
    suspend fun openManageSubscriptions()

    /**
     * Check premium status
     */
    suspend fun checkPremiumStatus(): Boolean

    /**
     * Get remaining free recordings for free users
     */
    suspend fun getRemainingFreeRecordings(): Int
} 