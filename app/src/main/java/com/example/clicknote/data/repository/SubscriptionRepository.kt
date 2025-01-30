package com.example.clicknote.data.repository

import com.example.clicknote.data.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    suspend fun createPaymentIntent(subscriptionType: String): String
    suspend fun createSubscription(paymentMethodId: String, subscriptionType: String): String
    suspend fun switchToFreePlan()
    suspend fun cancelSubscription()
    suspend fun getSubscriptionStatus(): SubscriptionStatus
    
    // Usage tracking
    fun observeSubscriptionStatus(): Flow<SubscriptionStatus>
    suspend fun incrementUsageCount()
    suspend fun resetWeeklyUsage()
    suspend fun canMakeTranscription(): Boolean
} 