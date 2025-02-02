package com.example.clicknote.domain.model

import kotlinx.coroutines.flow.StateFlow
import com.example.clicknote.domain.model.SubscriptionPlan

interface SubscriptionStateManager {
    val subscriptionState: StateFlow<SubscriptionStatus>
    val isPremium: StateFlow<Boolean>
    val weeklyRecordingsCount: StateFlow<Int>

    suspend fun updateSubscriptionState(plan: SubscriptionPlan)
    suspend fun resetSubscriptionState()
    suspend fun consumeFreeRecording()
    suspend fun getRemainingFreeRecordings(): Int
    suspend fun resetFreeRecordingsCount()
}

sealed class SubscriptionStatus {
    object Free : SubscriptionStatus()
    data class Premium(
        val expirationDate: Long,
        val isAutoRenewing: Boolean,
        val plan: SubscriptionPlan
    ) : SubscriptionStatus()
    object Loading : SubscriptionStatus()
    data class Error(val message: String) : SubscriptionStatus()
} 