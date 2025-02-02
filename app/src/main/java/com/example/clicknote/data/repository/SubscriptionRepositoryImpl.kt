package com.example.clicknote.data.repository

import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.repository.SubscriptionRepository
import com.example.clicknote.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    @ApplicationScope private val coroutineScope: CoroutineScope
) : SubscriptionRepository {

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _weeklyRecordingsCount = MutableStateFlow(3)
    override val weeklyRecordingsCount: StateFlow<Int> = _weeklyRecordingsCount.asStateFlow()

    private val _currentPlan = MutableStateFlow<SubscriptionPlan>(SubscriptionPlan.FREE)
    override val currentPlan: StateFlow<SubscriptionPlan> = _currentPlan.asStateFlow()

    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Free)
    override val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    override suspend fun updateSubscriptionState(plan: SubscriptionPlan) {
        _currentPlan.value = plan
        _isPremium.value = plan != SubscriptionPlan.FREE
        _subscriptionStatus.value = when (plan) {
            SubscriptionPlan.FREE -> SubscriptionStatus.Free
            SubscriptionPlan.MONTHLY -> SubscriptionStatus.Premium(
                expirationDate = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000,
                isAutoRenewing = true,
                plan = plan
            )
            SubscriptionPlan.ANNUAL -> SubscriptionStatus.Premium(
                expirationDate = System.currentTimeMillis() + 365L * 24 * 60 * 60 * 1000,
                isAutoRenewing = true,
                plan = plan
            )
        }
    }

    override suspend fun consumeFreeRecording() {
        if (!_isPremium.value && _weeklyRecordingsCount.value > 0) {
            _weeklyRecordingsCount.value = _weeklyRecordingsCount.value - 1
        }
    }

    override suspend fun resetWeeklyRecordings() {
        if (!_isPremium.value) {
            _weeklyRecordingsCount.value = 3
        }
    }

    override suspend fun cancelSubscription() {
        updateSubscriptionState(SubscriptionPlan.FREE)
    }

    override suspend fun restoreSubscription(plan: SubscriptionPlan) {
        updateSubscriptionState(plan)
    }
}