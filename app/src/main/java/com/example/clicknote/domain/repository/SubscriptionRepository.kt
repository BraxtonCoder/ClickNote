package com.example.clicknote.domain.repository

import com.example.clicknote.data.model.SubscriptionTier
import com.example.clicknote.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    val subscriptionStatus: Flow<SubscriptionStatus>
    
    suspend fun updateSubscriptionStatus(status: SubscriptionStatus)
    suspend fun checkSubscriptionStatus()
    suspend fun canRecordMore(): Boolean
    suspend fun getRemainingRecordings(): Int
    suspend fun consumeRecording()
    suspend fun subscribe(tier: SubscriptionTier, paymentMethodId: String)
    suspend fun cancelSubscription()
    suspend fun createPaymentIntent(tier: SubscriptionTier): String
}

enum class SubscriptionTier(val weeklyLimit: Int) {
    FREE(3),
    MONTHLY(Int.MAX_VALUE),
    ANNUAL(Int.MAX_VALUE)
} 