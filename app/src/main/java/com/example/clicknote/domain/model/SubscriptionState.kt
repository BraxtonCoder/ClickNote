package com.example.clicknote.domain.model

import kotlinx.coroutines.flow.StateFlow

interface SubscriptionStateManager {
    val subscriptionState: StateFlow<SubscriptionState>
    val isPremium: StateFlow<Boolean>
    
    suspend fun updateSubscriptionState(newState: SubscriptionState)
    suspend fun checkPremiumStatus()
    suspend fun resetSubscriptionState()
    suspend fun consumeFreeRecording()
    suspend fun resetFreeRecordingsCount()
}

/**
 * Represents the different subscription states in the app
 * @property price The price of the subscription in GBP
 * @property weeklyLimit The number of transcriptions allowed per week (null for unlimited)
 */
enum class SubscriptionState(val price: Float, val weeklyLimit: Int) {
    /**
     * Free plan with limited transcriptions
     */
    FREE(0f, 3),

    /**
     * Active monthly subscription
     */
    MONTHLY(9.99f, Int.MAX_VALUE),

    /**
     * Active annual subscription
     */
    ANNUAL(98f, Int.MAX_VALUE),

    /**
     * Error occurred while checking subscription status
     */
    ERROR(0f, 0);

    fun isPremium() = this == MONTHLY || this == ANNUAL
} 