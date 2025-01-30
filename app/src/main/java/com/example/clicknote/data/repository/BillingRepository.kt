package com.example.clicknote.data.repository

import com.example.clicknote.domain.model.SubscriptionState
import kotlinx.coroutines.flow.Flow

interface BillingRepository {
    val subscriptionState: Flow<SubscriptionState>
    
    suspend fun checkSubscriptionStatus()
    suspend fun purchaseSubscription(type: SubscriptionState)
    suspend fun cancelSubscription()
    suspend fun restorePurchases()
    suspend fun getRemainingFreeNotes(): Int
} 