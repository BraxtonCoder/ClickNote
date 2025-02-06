package com.example.clicknote.service.billing

import android.app.Activity
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import kotlinx.coroutines.flow.StateFlow

interface BillingService {
    val subscriptionStatus: StateFlow<SubscriptionStatus>
    
    suspend fun initializeBillingClient()
    suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan)
    suspend fun queryPurchases()
    fun endConnection()
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