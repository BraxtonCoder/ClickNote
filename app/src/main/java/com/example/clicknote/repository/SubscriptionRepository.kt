package com.example.clicknote.repository

import com.example.clicknote.model.SubscriptionState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepository @Inject constructor() {
    private val _subscriptionState = MutableStateFlow<SubscriptionState>(SubscriptionState.FREE)
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _freeRecordingsRemaining = MutableStateFlow(3)
    val freeRecordingsRemaining: StateFlow<Int> = _freeRecordingsRemaining.asStateFlow()

    fun updateSubscriptionState(newState: SubscriptionState) {
        _subscriptionState.value = newState
        _isPremium.value = newState != SubscriptionState.FREE
    }

    fun consumeFreeRecording() {
        if (!_isPremium.value && _freeRecordingsRemaining.value > 0) {
            _freeRecordingsRemaining.value = _freeRecordingsRemaining.value - 1
        }
    }

    fun resetFreeRecordings() {
        if (!_isPremium.value) {
            _freeRecordingsRemaining.value = 3
        }
    }

    fun hasRemainingFreeRecordings(): Boolean {
        return _isPremium.value || _freeRecordingsRemaining.value > 0
    }
} 