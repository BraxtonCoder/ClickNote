package com.example.clicknote.service

import com.example.clicknote.domain.model.SubscriptionState
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.analytics.AnalyticsService
import kotlinx.coroutines.flow.*
import java.time.DayOfWeek
import java.time.LocalDateTime
import java.time.temporal.WeekFields
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PremiumFeatureManager @Inject constructor(
    private val subscriptionService: Lazy<SubscriptionService>,
    private val userPreferences: Lazy<UserPreferencesDataStore>,
    private val analyticsService: Lazy<AnalyticsService>
) {
    private val _weeklyTranscriptionCount = MutableStateFlow(0)
    val weeklyTranscriptionCount = _weeklyTranscriptionCount.asStateFlow()

    init {
        loadWeeklyCount()
    }

    private fun loadWeeklyCount() {
        _weeklyTranscriptionCount.value = userPreferences.get().getWeeklyTranscriptionCount()
    }

    suspend fun canUseFeature(feature: PremiumFeature): Boolean {
        val subscriptionState = subscriptionService.get().subscriptionState.first()
        
        return when {
            subscriptionState == SubscriptionState.MONTHLY || subscriptionState == SubscriptionState.ANNUAL -> {
                analyticsService.get().trackFeatureAccess(feature.name, "premium")
                true
            }
            feature.availableInFree -> {
                val count = userPreferences.get().getWeeklyTranscriptionCount()
                val canUse = count < MAX_FREE_TRANSCRIPTIONS
                analyticsService.get().trackFeatureAccess(feature.name, if (canUse) "free" else "limit_reached")
                canUse
            }
            else -> {
                analyticsService.get().trackFeatureAccess(feature.name, "premium_required")
                false
            }
        }
    }

    suspend fun incrementTranscriptionCount() {
        if (subscriptionService.get().subscriptionState.first() == SubscriptionState.FREE) {
            val newCount = _weeklyTranscriptionCount.value + 1
            _weeklyTranscriptionCount.value = newCount
            userPreferences.get().setWeeklyTranscriptionCount(newCount)
            
            analyticsService.get().trackWeeklyLimitUpdate(
                newCount = newCount,
                remainingCount = MAX_FREE_TRANSCRIPTIONS - newCount
            )
        }
    }

    fun resetWeeklyCount() {
        _weeklyTranscriptionCount.value = 0
        userPreferences.get().setWeeklyTranscriptionCount(0)
        analyticsService.get().trackWeeklyLimitUpdate(
            newCount = 0,
            remainingCount = MAX_FREE_TRANSCRIPTIONS
        )
    }

    fun getRemainingTranscriptions(): Int {
        return MAX_FREE_TRANSCRIPTIONS - _weeklyTranscriptionCount.value
    }

    companion object {
        const val MAX_FREE_TRANSCRIPTIONS = 3
    }
}

enum class PremiumFeature {
    TRANSCRIPTION,
    CLOUD_SYNC,
    CALL_RECORDING,
    MULTI_SPEAKER,
    AI_SUMMARY,
    AUDIO_ENHANCEMENT,
    OFFLINE_MODE
} 