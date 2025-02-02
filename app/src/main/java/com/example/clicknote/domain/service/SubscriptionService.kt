package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.StateFlow

interface SubscriptionService {
    val isPremium: StateFlow<Boolean>
    val weeklyRecordingsCount: StateFlow<Int>
    val currentPlan: StateFlow<SubscriptionPlan>
    val subscriptionStatus: StateFlow<SubscriptionStatus>

    suspend fun purchaseSubscription(plan: SubscriptionPlan)
    suspend fun updateSubscriptionState(plan: SubscriptionPlan)
    suspend fun consumeFreeRecording()
    suspend fun resetWeeklyRecordings()
    suspend fun cancelSubscription()
    suspend fun restoreSubscription(): Result<Unit>
    suspend fun checkSubscriptionStatus()
    suspend fun getRemainingFreeTranscriptions(): Int
    suspend fun decrementFreeTranscriptions()
    suspend fun isSubscriptionActive(): Boolean
    suspend fun getSubscriptionExpiration(): Long?
} 