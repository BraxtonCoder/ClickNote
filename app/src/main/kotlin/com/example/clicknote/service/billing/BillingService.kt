package com.example.clicknote.service.billing

import kotlinx.coroutines.flow.Flow

interface BillingService {
    suspend fun initialize()
    suspend fun querySubscriptionPlans(): List<SubscriptionPlan>
    suspend fun subscribe(plan: SubscriptionPlan)
    suspend fun unsubscribe()
    suspend fun restorePurchases()
    fun observeSubscriptionStatus(): Flow<SubscriptionStatus>
    suspend fun isSubscriptionActive(): Boolean
    suspend fun getRemainingUsage(): Int
    suspend fun consumeUsage()
}

data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val period: String,
    val features: List<String>,
    val isRecommended: Boolean = false
)

data class SubscriptionStatus(
    val isActive: Boolean = false,
    val plan: SubscriptionPlan? = null,
    val startDate: Long? = null,
    val endDate: Long? = null,
    val isGracePeriod: Boolean = false,
    val gracePeriodEndDate: Long? = null,
    val weeklyUsageCount: Int = 0,
    val weeklyUsageLimit: Int = 3
) 