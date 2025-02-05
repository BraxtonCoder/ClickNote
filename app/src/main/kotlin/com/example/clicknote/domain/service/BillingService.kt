package com.example.clicknote.domain.service

import android.app.Activity
import com.example.clicknote.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.StateFlow

interface BillingService {
    val subscriptionStatus: StateFlow<SubscriptionPlan>
    suspend fun initializeBillingClient()
    suspend fun launchBillingFlow(activity: Activity, plan: SubscriptionPlan)
    suspend fun queryPurchases()
    fun endConnection()
} 