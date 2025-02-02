package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.service.SubscriptionService
import com.example.clicknote.domain.repository.SubscriptionRepository
import com.example.clicknote.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val subscriptionRepository: SubscriptionRepository,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : SubscriptionService {

    override val isPremium: StateFlow<Boolean> = subscriptionRepository.isPremium
    override val weeklyRecordingsCount: StateFlow<Int> = subscriptionRepository.weeklyRecordingsCount
    override val currentPlan: StateFlow<SubscriptionPlan> = subscriptionRepository.currentPlan
    override val subscriptionStatus: StateFlow<SubscriptionStatus> = subscriptionRepository.subscriptionStatus

    override suspend fun purchaseSubscription(plan: SubscriptionPlan) {
        // This is now handled by BillingService directly
        // The purchase will be processed through the BillingService when completed
    }

    override suspend fun updateSubscriptionState(plan: SubscriptionPlan) {
        subscriptionRepository.updateSubscriptionState(plan)
    }

    override suspend fun consumeFreeRecording() {
        subscriptionRepository.consumeFreeRecording()
    }

    override suspend fun resetWeeklyRecordings() {
        subscriptionRepository.resetWeeklyRecordings()
    }

    override suspend fun cancelSubscription() {
        subscriptionRepository.cancelSubscription()
    }

    override suspend fun restoreSubscription(plan: SubscriptionPlan) {
        subscriptionRepository.restoreSubscription(plan)
    }

    override suspend fun checkSubscriptionStatus() {
        // This is handled by BillingService through subscription querying
    }

    override suspend fun getRemainingFreeTranscriptions(): Int {
        return weeklyRecordingsCount.value
    }

    override suspend fun decrementFreeTranscriptions() {
        if (currentPlan.value == SubscriptionPlan.FREE) {
            consumeFreeRecording()
        }
    }

    override suspend fun isSubscriptionActive(): Boolean {
        return isPremium.value
    }

    override suspend fun getSubscriptionExpiration(): Long? {
        return when (val status = subscriptionStatus.value) {
            is SubscriptionStatus.Premium -> status.expirationDate
            else -> null
        }
    }

    companion object {
        private const val MONTHLY_SUBSCRIPTION_ID = "monthly_subscription"
        private const val ANNUAL_SUBSCRIPTION_ID = "annual_subscription"
    }
} 