package com.example.clicknote.service

import com.example.clicknote.domain.model.SubscriptionState
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

interface SubscriptionStateManager {
    val subscriptionState: StateFlow<SubscriptionState>
    val isPremium: StateFlow<Boolean>
    val weeklyRecordingsCount: StateFlow<Int>
    
    suspend fun updateSubscriptionState(newState: SubscriptionState)
    suspend fun checkPremiumStatus()
    suspend fun resetSubscriptionState()
    suspend fun consumeFreeRecording()
    suspend fun resetFreeRecordingsCount()
    suspend fun getRemainingFreeRecordings(): Int
}

@Singleton
class SubscriptionStateManagerImpl @Inject constructor(
    private val userPreferences: Provider<UserPreferencesDataStore>
) : SubscriptionStateManager {
    private val _subscriptionState = MutableStateFlow(SubscriptionState.FREE)
    override val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _weeklyRecordingsCount = MutableStateFlow(SubscriptionState.FREE.weeklyLimit)
    override val weeklyRecordingsCount: StateFlow<Int> = _weeklyRecordingsCount.asStateFlow()

    override suspend fun updateSubscriptionState(newState: SubscriptionState) {
        _subscriptionState.value = newState
        _isPremium.value = newState.isPremium()
        if (newState == SubscriptionState.FREE) {
            _weeklyRecordingsCount.value = newState.weeklyLimit
        }
        userPreferences.get().updateSubscriptionState(newState)
    }

    override suspend fun checkPremiumStatus() {
        val currentState = userPreferences.get().getSubscriptionState()
        _subscriptionState.value = currentState ?: SubscriptionState.FREE
        _isPremium.value = currentState?.isPremium() ?: false
    }

    override suspend fun resetSubscriptionState() {
        updateSubscriptionState(SubscriptionState.FREE)
    }

    override suspend fun getRemainingFreeRecordings(): Int {
        return userPreferences.get().getWeeklyTranscriptionCount()
    }

    override suspend fun consumeFreeRecording() {
        if (_subscriptionState.value == SubscriptionState.FREE) {
            val currentCount = getRemainingFreeRecordings()
            userPreferences.get().setWeeklyTranscriptionCount(currentCount - 1)
            _weeklyRecordingsCount.value = currentCount - 1
        }
    }

    override suspend fun resetFreeRecordingsCount() {
        if (_subscriptionState.value == SubscriptionState.FREE) {
            val limit = SubscriptionState.FREE.weeklyLimit
            userPreferences.get().setWeeklyTranscriptionCount(limit)
            _weeklyRecordingsCount.value = limit
        }
    }
} 