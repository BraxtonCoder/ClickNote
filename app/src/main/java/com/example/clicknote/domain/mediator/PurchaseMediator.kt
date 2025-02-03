package com.example.clicknote.domain.mediator

import kotlinx.coroutines.flow.Flow

interface PurchaseMediator {
    suspend fun initializeBilling()
    suspend fun querySubscriptions(): Flow<List<String>>
    suspend fun launchBillingFlow(productId: String)
    suspend fun checkSubscriptionStatus(): Boolean
    suspend fun endBillingConnection()
    fun getWeeklyTranscriptionCount(): Int
    fun incrementWeeklyTranscriptionCount()
    fun resetWeeklyTranscriptionCount()
} 