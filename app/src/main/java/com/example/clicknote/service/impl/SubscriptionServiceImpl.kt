package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionState
import com.example.clicknote.domain.model.SubscriptionStatus
import com.example.clicknote.domain.model.SubscriptionTier
import com.example.clicknote.domain.repository.UserRepository
import com.example.clicknote.service.BillingService
import com.example.clicknote.service.SubscriptionService
import com.example.clicknote.service.api.StripeApi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userRepository: UserRepository,
    private val billingService: BillingService,
    private val stripeApi: StripeApi
) : SubscriptionService {

    override val subscriptionState: StateFlow<SubscriptionState> = billingService.subscriptionState
    override val isPremium: StateFlow<Boolean> = billingService.isPremium
    override val remainingFreeRecordings: Flow<Int> = billingService.remainingTranscriptions

    override suspend fun purchaseMonthlySubscription() {
        billingService.purchaseMonthlySubscription()
    }

    override suspend fun purchaseAnnualSubscription() {
        billingService.purchaseAnnualSubscription()
    }

    override suspend fun restorePurchases() {
        billingService.restorePurchases()
    }

    override suspend fun hasActiveSubscription(): Boolean {
        return billingService.hasActiveSubscription()
    }

    override suspend fun checkTranscriptionAvailability(): Result<Boolean> {
        return billingService.checkTranscriptionAvailability()
    }

    override suspend fun incrementTranscriptionCount(): Result<Unit> {
        return billingService.incrementTranscriptionCount()
    }

    override suspend fun resetTranscriptionCount(): Result<Unit> {
        return billingService.resetTranscriptionCount()
    }

    override suspend fun showSubscriptionOptions() {
        billingService.showSubscriptionOptions()
    }

    override suspend fun manageSubscription() {
        billingService.manageSubscription()
    }

    override suspend fun endBillingConnection() {
        billingService.endBillingConnection()
    }

    override fun isPremiumUser(): Boolean {
        return billingService.isPremiumUser()
    }

    override suspend fun getRemainingFreeRecordings(): Int {
        return billingService.getRemainingFreeRecordings()
    }

    override suspend fun consumeFreeRecording() {
        billingService.consumeFreeRecording()
    }

    override fun resetFreeRecordingsCount() {
        billingService.resetFreeRecordingsCount()
    }

    override suspend fun getCurrentPlan(): SubscriptionPlan {
        return when (billingService.subscriptionState.value) {
            SubscriptionState.MONTHLY -> SubscriptionPlan.MONTHLY
            SubscriptionState.ANNUAL -> SubscriptionPlan.ANNUAL
            else -> SubscriptionPlan.FREE
        }
    }

    override suspend fun subscribeToPlan(planId: String): Result<Unit> {
        return try {
            when (planId) {
                "monthly" -> billingService.purchaseMonthlySubscription()
                "annual" -> billingService.purchaseAnnualSubscription()
                else -> throw IllegalArgumentException("Invalid plan ID")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelSubscription(): Result<Unit> {
        return try {
            billingService.endBillingConnection()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getSubscriptionStatus(): Flow<SubscriptionStatus> {
        return billingService.subscriptionState.map { state ->
            val now = LocalDateTime.now()
            SubscriptionStatus(
                tier = when (state) {
                    SubscriptionState.MONTHLY -> SubscriptionTier.MONTHLY
                    SubscriptionState.ANNUAL -> SubscriptionTier.ANNUAL
                    else -> SubscriptionTier.FREE
                },
                isActive = state == SubscriptionState.MONTHLY || state == SubscriptionState.ANNUAL,
                startDate = if (state == SubscriptionState.MONTHLY || state == SubscriptionState.ANNUAL) 
                    now else null,
                endDate = if (state == SubscriptionState.MONTHLY || state == SubscriptionState.ANNUAL)
                    now.plusMonths(if (state == SubscriptionState.MONTHLY) 1 else 12) else null,
                weeklyUsageCount = if (state == SubscriptionState.FREE) billingService.getRemainingFreeRecordings() else 0,
                weeklyResetDate = if (state == SubscriptionState.FREE) now.plusDays(7) else null,
                subscriptionEndDate = if (state == SubscriptionState.MONTHLY || state == SubscriptionState.ANNUAL)
                    now.plusMonths(if (state == SubscriptionState.MONTHLY) 1 else 12) else null
            )
        }
    }

    override suspend fun getSubscriptionExpiration(): Long? {
        val status = getSubscriptionStatus().first()
        return status.subscriptionEndDate?.toEpochSecond(ZoneOffset.UTC)
    }
} 