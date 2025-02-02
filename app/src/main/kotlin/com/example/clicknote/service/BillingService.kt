package com.example.clicknote.service

import android.app.Activity
import com.example.clicknote.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface BillingService {
    /**
     * Initialize billing connection
     */
    fun initializeBillingClient(activity: Activity)

    /**
     * End billing connection
     */
    fun endBillingConnection()

    /**
     * Purchase a subscription plan
     */
    suspend fun purchaseSubscription(plan: SubscriptionPlan): Result<Unit>

    /**
     * Restore purchases
     */
    suspend fun restorePurchases(): Result<Unit>

    /**
     * Get current subscription plan
     */
    val currentPlan: StateFlow<SubscriptionPlan>

    /**
     * Get remaining free recordings
     */
    val remainingFreeRecordings: Flow<Int>
} 