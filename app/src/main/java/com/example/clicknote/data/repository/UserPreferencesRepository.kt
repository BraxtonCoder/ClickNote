package com.example.clicknote.data.repository

import kotlinx.coroutines.flow.Flow

interface UserPreferencesRepository {
    val isPremium: Flow<Boolean>
    val weeklyTranscriptionCount: Flow<Int>
    
    suspend fun updatePremiumStatus(isPremium: Boolean)
    suspend fun incrementWeeklyTranscriptionCount()
    suspend fun resetWeeklyTranscriptionCount()
} 