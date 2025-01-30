package com.example.clicknote.data.preferences

import kotlinx.coroutines.flow.Flow

interface UserPreferences {
    fun getWeeklyUsageCount(): Flow<Int>
    suspend fun incrementWeeklyUsageCount()
    suspend fun resetWeeklyUsageCount()
    
    fun getLastWeeklyResetDate(): Flow<Long>
    suspend fun updateLastWeeklyResetDate(timestamp: Long)
    
    fun getSubscriptionId(): Flow<String?>
    suspend fun setSubscriptionId(id: String?)
    
    fun isSubscriptionActive(): Flow<Boolean>
    suspend fun setSubscriptionActive(active: Boolean)
} 