package com.example.clicknote.domain.model

import kotlinx.coroutines.flow.StateFlow

/**
 * Interface for managing subscription state in the app
 */
interface SubscriptionStateManager {
    val currentPlan: StateFlow<SubscriptionPlan>
    val subscriptionStatus: StateFlow<SubscriptionStatus>
    val weeklyRecordingsCount: StateFlow<Int>
    val isPremium: StateFlow<Boolean>

    suspend fun updateSubscriptionState(plan: SubscriptionPlan)
    suspend fun consumeFreeRecording()
    suspend fun resetWeeklyRecordings()
    suspend fun cancelSubscription()
    suspend fun restoreSubscription(plan: SubscriptionPlan)
    suspend fun isSubscriptionActive(): Boolean
} 