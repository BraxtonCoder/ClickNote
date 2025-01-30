package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

interface SubscriptionService {
    suspend fun getCurrentPlan(): SubscriptionPlan
    suspend fun subscribeToPlan(planId: String): Result<Unit>
    suspend fun cancelSubscription(): Result<Unit>
    suspend fun restorePurchases(): Result<Unit>
    fun getSubscriptionStatus(): Flow<SubscriptionStatus>
    suspend fun getRemainingFreeTranscriptions(): Int
    suspend fun decrementFreeTranscriptions()
    suspend fun isSubscriptionActive(): Boolean
    suspend fun getSubscriptionExpiration(): Long?
} 