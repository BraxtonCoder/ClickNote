package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Purchase
import com.example.clicknote.domain.model.SubscriptionPlan
import kotlinx.coroutines.flow.Flow

interface PurchaseRepository {
    suspend fun savePurchase(purchase: Purchase)
    suspend fun getPurchase(purchaseId: String): Purchase?
    suspend fun getCurrentPlan(): Flow<SubscriptionPlan>
    suspend fun getWeeklyTranscriptionCount(): Int
    suspend fun incrementWeeklyTranscriptionCount()
    suspend fun resetWeeklyTranscriptionCount()
    suspend fun getPurchaseHistory(): Flow<List<Purchase>>
    suspend fun cancelSubscription(purchaseId: String)
    suspend fun refundPurchase(purchaseId: String)
} 