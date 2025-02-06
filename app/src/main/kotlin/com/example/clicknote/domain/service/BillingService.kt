package com.example.clicknote.domain.service

import android.app.Activity
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.StateFlow

interface BillingService {
    /**
     * Current subscription status
     */
    val subscriptionStatus: StateFlow<SubscriptionStatus>

    /**
     * Initialize the billing client
     */
    suspend fun initializeBillingClient()

    /**
     * Launch billing flow for subscription purchase
     */
    suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan)

    /**
     * Query current purchases
     */
    suspend fun queryPurchases()

    /**
     * End billing connection
     */
    fun endConnection()

    /**
     * Check if user can make a transcription
     */
    suspend fun canMakeTranscription(): Boolean

    /**
     * Get remaining free transcriptions
     */
    suspend fun getRemainingTranscriptions(): Int

    /**
     * Consume a free transcription
     */
    suspend fun consumeTranscription()

    /**
     * Reset transcription count
     */
    suspend fun resetTranscriptionCount()

    /**
     * Check if user has premium subscription
     */
    suspend fun isPremium(): Boolean

    companion object {
        const val MONTHLY_SUBSCRIPTION_ID = "clicknote_monthly_subscription"
        const val ANNUAL_SUBSCRIPTION_ID = "clicknote_annual_subscription"
        const val FREE_WEEKLY_LIMIT = 3
    }
} 