package com.example.clicknote.service

import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class SubscriptionStateManagerImpl @Inject constructor(
    private val userPreferences: Lazy<UserPreferencesDataStore>
) : SubscriptionStateManager {
    private val _subscriptionState = MutableStateFlow(SubscriptionState.FREE)
    override val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState

    override suspend fun updateSubscriptionState(state: SubscriptionState) {
        _subscriptionState.value = state
        userPreferences.get().updateSubscriptionState(state)
    }

    override suspend fun checkSubscriptionStatus() {
        val currentState = userPreferences.get().getSubscriptionState()
        _subscriptionState.value = currentState
    }

    override suspend fun resetSubscriptionState() {
        updateSubscriptionState(SubscriptionState.FREE)
    }

    override suspend fun getRemainingFreeRecordings(): Int {
        return userPreferences.get().getWeeklyTranscriptionCount()
    }

    override suspend fun consumeFreeRecording() {
        val currentCount = getRemainingFreeRecordings()
        userPreferences.get().setWeeklyTranscriptionCount(currentCount - 1)
    }
} 