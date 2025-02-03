package com.example.clicknote.domain.interfaces

interface PurchaseMediator {
    suspend fun initializeBilling()
    suspend fun querySubscriptions(): List<String>
    suspend fun purchaseSubscription(subscriptionId: String)
    suspend fun checkSubscriptionStatus(): Boolean
    suspend fun restorePurchases()
    suspend fun endBillingConnection()
} 