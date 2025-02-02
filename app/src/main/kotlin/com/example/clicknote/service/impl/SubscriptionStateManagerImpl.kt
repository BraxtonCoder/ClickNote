package com.example.clicknote.service.impl

import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.model.SubscriptionStateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionStateManagerImpl @Inject constructor() : SubscriptionStateManager {

    private val _subscriptionState = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Free)
    override val subscriptionState: StateFlow<SubscriptionStatus> = _subscriptionState.asStateFlow()

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _weeklyRecordingsCount = MutableStateFlow(3)
    override val weeklyRecordingsCount: StateFlow<Int> = _weeklyRecordingsCount.asStateFlow()

    override suspend fun updateSubscriptionState(plan: SubscriptionPlan) {
        when (plan) {
            SubscriptionPlan.FREE -> {
                _subscriptionState.value = SubscriptionStatus.Free
                _isPremium.value = false
                _weeklyRecordingsCount.value = 3
            }
            SubscriptionPlan.MONTHLY, SubscriptionPlan.ANNUAL -> {
                val status = SubscriptionStatus.Premium(
                    expirationDate = System.currentTimeMillis() + (if (plan == SubscriptionPlan.MONTHLY) 30L else 365L) * 24 * 60 * 60 * 1000,
                    isAutoRenewing = true,
                    plan = plan
                )
                _subscriptionState.value = status
                _isPremium.value = true
            }
        }
    }

    override suspend fun resetSubscriptionState() {
        _subscriptionState.value = SubscriptionStatus.Free
        _isPremium.value = false
        _weeklyRecordingsCount.value = 3
    }

    override suspend fun consumeFreeRecording() {
        if (_weeklyRecordingsCount.value > 0) {
            _weeklyRecordingsCount.value = _weeklyRecordingsCount.value - 1
        }
    }

    override suspend fun resetFreeRecordingsCount() {
        _weeklyRecordingsCount.value = 3
    }

    override suspend fun getRemainingFreeRecordings(): Int {
        return _weeklyRecordingsCount.value
    }
} 