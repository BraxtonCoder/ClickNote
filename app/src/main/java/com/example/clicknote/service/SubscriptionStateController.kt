package com.example.clicknote.service

import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class SubscriptionStateController @Inject constructor(
    private val userPreferences: Lazy<UserPreferencesDataStore>
) {
    private val _subscriptionState = MutableStateFlow(SubscriptionState.FREE)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    init {
        // Initialize subscription state from preferences
        userPreferences.get().getSubscriptionState()?.let { state ->
            _subscriptionState.value = state
            _isPremium.value = state == SubscriptionState.MONTHLY || state == SubscriptionState.ANNUAL
        }
    }

    suspend fun updateSubscriptionState(state: SubscriptionState) {
        _subscriptionState.value = state
        _isPremium.value = state == SubscriptionState.MONTHLY || state == SubscriptionState.ANNUAL
        userPreferences.get().updateSubscriptionState(state)
    }

    suspend fun checkSubscriptionStatus() {
        val currentState = userPreferences.get().getSubscriptionState()
        _subscriptionState.value = currentState ?: SubscriptionState.FREE
        _isPremium.value = currentState == SubscriptionState.MONTHLY || currentState == SubscriptionState.ANNUAL
    }

    suspend fun resetSubscriptionState() {
        updateSubscriptionState(SubscriptionState.FREE)
    }

    fun consumeFreeRecording() {
        if (_subscriptionState.value == SubscriptionState.FREE) {
            val currentCount = userPreferences.get().getWeeklyTranscriptionCount()
            if (currentCount > 0) {
                userPreferences.get().setWeeklyTranscriptionCount(currentCount - 1)
            }
        }
    }

    fun resetFreeRecordingsCount() {
        if (_subscriptionState.value == SubscriptionState.FREE) {
            userPreferences.get().setWeeklyTranscriptionCount(3)
        }
    }
} 