package com.example.clicknote.data.repository

import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.SubscriptionRepository
import com.example.clicknote.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

private const val FREE_WEEKLY_LIMIT = 3
private const val MONTHLY_DURATION = 30L * 24 * 60 * 60 * 1000 // 30 days in milliseconds
private const val ANNUAL_DURATION = 365L * 24 * 60 * 60 * 1000 // 365 days in milliseconds

@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val preferences: UserPreferencesDataStore
) : SubscriptionRepository {

    private val _isPremium = MutableStateFlow(false)
    override val isPremium: StateFlow<Boolean> = _isPremium.asStateFlow()

    private val _weeklyRecordingsCount = MutableStateFlow(FREE_WEEKLY_LIMIT)
    override val weeklyRecordingsCount: StateFlow<Int> = _weeklyRecordingsCount.asStateFlow()

    private val _currentPlan = MutableStateFlow<SubscriptionPlan>(SubscriptionPlan.Free)
    override val currentPlan: StateFlow<SubscriptionPlan> = _currentPlan.asStateFlow()

    private val _subscriptionStatus = MutableStateFlow<SubscriptionStatus>(SubscriptionStatus.Free)
    override val subscriptionStatus: StateFlow<SubscriptionStatus> = _subscriptionStatus.asStateFlow()

    override suspend fun updateSubscriptionState(plan: SubscriptionPlan) {
        _currentPlan.value = plan
        _isPremium.value = plan != SubscriptionPlan.Free
        
        _subscriptionStatus.value = when (plan) {
            SubscriptionPlan.Free -> {
                _weeklyRecordingsCount.value = FREE_WEEKLY_LIMIT
                SubscriptionStatus.Free
            }
            SubscriptionPlan.Monthly -> SubscriptionStatus.Premium(
                expirationDate = System.currentTimeMillis() + MONTHLY_DURATION,
                isAutoRenewing = true,
                plan = plan
            )
            SubscriptionPlan.Annual -> SubscriptionStatus.Premium(
                expirationDate = System.currentTimeMillis() + ANNUAL_DURATION,
                isAutoRenewing = true,
                plan = plan
            )
        }
        preferences.setSubscriptionStatus(_subscriptionStatus.value)
    }

    override suspend fun consumeFreeRecording() {
        if (!_isPremium.value && _weeklyRecordingsCount.value > 0) {
            _weeklyRecordingsCount.value = _weeklyRecordingsCount.value - 1
            preferences.incrementWeeklyTranscriptionCount()
        }
    }

    override suspend fun resetWeeklyRecordings() {
        if (!_isPremium.value) {
            _weeklyRecordingsCount.value = FREE_WEEKLY_LIMIT
            preferences.resetWeeklyTranscriptionCount()
        }
    }

    override suspend fun cancelSubscription() {
        _subscriptionStatus.value = SubscriptionStatus.Cancelled
        _currentPlan.value = SubscriptionPlan.Free
        _isPremium.value = false
        _weeklyRecordingsCount.value = FREE_WEEKLY_LIMIT
        preferences.setSubscriptionStatus(_subscriptionStatus.value)
    }

    override suspend fun restoreSubscription(plan: SubscriptionPlan) {
        // Check stored subscription status from preferences
        val storedStatus = preferences.subscriptionStatus.first()
        if (storedStatus is SubscriptionStatus.Premium && storedStatus.expirationDate > System.currentTimeMillis()) {
            _subscriptionStatus.value = storedStatus
            _currentPlan.value = storedStatus.plan
            _isPremium.value = true
        } else {
            // If expired or invalid, reset to free
            updateSubscriptionState(plan)
        }
    }

    override suspend fun isSubscriptionActive(): Boolean {
        val currentStatus = _subscriptionStatus.value
        return when (currentStatus) {
            is SubscriptionStatus.Premium -> currentStatus.expirationDate > System.currentTimeMillis()
            else -> false
        }
    }
}