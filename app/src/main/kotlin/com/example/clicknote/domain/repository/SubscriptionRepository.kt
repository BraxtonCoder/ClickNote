package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.StateFlow

interface SubscriptionRepository {
    val isPremium: StateFlow<Boolean>
    val weeklyRecordingsCount: StateFlow<Int>
    val currentPlan: StateFlow<SubscriptionPlan>
    val subscriptionStatus: StateFlow<SubscriptionStatus>

    suspend fun updateSubscriptionState(plan: SubscriptionPlan)
    suspend fun consumeFreeRecording()
    suspend fun resetWeeklyRecordings()
    suspend fun cancelSubscription()
    suspend fun restoreSubscription(plan: SubscriptionPlan)
    suspend fun isSubscriptionActive(): Boolean
}