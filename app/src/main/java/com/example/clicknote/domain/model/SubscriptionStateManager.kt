package com.example.clicknote.domain.model

import kotlinx.coroutines.flow.StateFlow

interface SubscriptionStateManager {
    val subscriptionState: StateFlow<SubscriptionState>
    val isPremium: StateFlow<Boolean>
    val weeklyRecordingsCount: StateFlow<Int>
    
    suspend fun updateSubscriptionState(newState: SubscriptionState)
    suspend fun checkPremiumStatus()
    suspend fun resetSubscriptionState()
    suspend fun consumeFreeRecording()
    suspend fun resetFreeRecordingsCount()
} 