package com.example.clicknote.service

import com.example.clicknote.domain.model.PremiumFeature
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.service.SubscriptionService
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumFeatureManager @Inject constructor(
    private val subscriptionService: SubscriptionService
) {
    suspend fun canUseFeature(featureId: String): Boolean {
        val feature = PremiumFeature.get(featureId) ?: return false
        val currentPlan = subscriptionService.currentPlan.first()
        val isPremium = subscriptionService.isPremium.first()

        return when {
            isPremium -> true
            feature.availableInFree -> {
                if (feature.freeUsageLimit > 0) {
                    val remainingUsage = subscriptionService.getRemainingFreeTranscriptions()
                    remainingUsage > 0
                } else {
                    true
                }
            }
            else -> false
        }
    }

    suspend fun checkSubscriptionStatus(): Boolean {
        return subscriptionService.isSubscriptionActive()
    }

    suspend fun getRemainingFreeUsage(): Int {
        return subscriptionService.getRemainingFreeTranscriptions()
    }

    suspend fun consumeFreeUsage() {
        subscriptionService.consumeFreeRecording()
    }

    suspend fun resetFreeUsage() {
        subscriptionService.resetWeeklyRecordings()
    }
} 