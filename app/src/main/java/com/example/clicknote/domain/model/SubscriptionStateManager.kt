package com.example.clicknote.domain.model

import kotlinx.coroutines.flow.StateFlow

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

enum class SubscriptionPlan(val price: Float, val weeklyLimit: Int) {
    FREE(0f, 3),
    MONTHLY(9.99f, Int.MAX_VALUE),
    ANNUAL(98f, Int.MAX_VALUE);

    fun isPremium() = this == MONTHLY || this == ANNUAL
} 