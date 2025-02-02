package com.example.clicknote.domain.service

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.example.clicknote.domain.model.SubscriptionPlan

interface BillingService {
    fun initializeBillingClient(activity: Activity)
    fun endBillingConnection()
    fun setPurchaseCallback(callback: (Purchase) -> Unit)
    suspend fun purchaseSubscription(plan: SubscriptionPlan)
    suspend fun querySubscriptions()
    suspend fun restorePurchases(): Result<Unit>
} 