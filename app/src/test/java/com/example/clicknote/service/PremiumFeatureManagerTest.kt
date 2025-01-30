package com.example.clicknote.service

import com.example.clicknote.domain.model.SubscriptionState
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.analytics.AnalyticsService
import io.mockk.*
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PremiumFeatureManagerTest {

    @MockK
    private lateinit var subscriptionService: SubscriptionService

    @MockK
    private lateinit var userPreferences: UserPreferencesDataStore

    @MockK
    private lateinit var analyticsService: AnalyticsService

    private lateinit var premiumFeatureManager: PremiumFeatureManager

    @Before
    fun setup() {
        MockKAnnotations.init(this)
        premiumFeatureManager = PremiumFeatureManager(
            subscriptionService = subscriptionService,
            userPreferences = userPreferences,
            analyticsService = analyticsService
        )

        // Default mocks
        every { userPreferences.getWeeklyTranscriptionCount() } returns 0
        every { userPreferences.setWeeklyTranscriptionCount(any()) } just Runs
        every { analyticsService.trackFeatureAccess(any(), any(), any()) } just Runs
        every { analyticsService.trackWeeklyLimitUpdate(any(), any()) } just Runs
    }

    @Test
    fun `canUseFeature returns true for premium users`() = runTest {
        // Given
        val subscriptionFlow = MutableStateFlow<SubscriptionState>(SubscriptionState.MONTHLY)
        every { subscriptionService.subscriptionState } returns subscriptionFlow

        // When
        val result = premiumFeatureManager.canUseFeature(PremiumFeature.TRANSCRIPTION)

        // Then
        assertTrue(result)
        verify {
            analyticsService.trackFeatureAccess(
                feature = PremiumFeature.TRANSCRIPTION.name,
                isAllowed = true,
                subscriptionState = SubscriptionState.MONTHLY
            )
        }
    }

    @Test
    fun `canUseFeature returns false for free users when weekly limit reached`() = runTest {
        // Given
        val subscriptionFlow = MutableStateFlow<SubscriptionState>(SubscriptionState.FREE)
        every { subscriptionService.subscriptionState } returns subscriptionFlow
        every { userPreferences.getWeeklyTranscriptionCount() } returns 3

        // When
        val result = premiumFeatureManager.canUseFeature(PremiumFeature.TRANSCRIPTION)

        // Then
        assertFalse(result)
        verify {
            analyticsService.trackFeatureAccess(
                feature = PremiumFeature.TRANSCRIPTION.name,
                isAllowed = false,
                subscriptionState = SubscriptionState.FREE
            )
        }
    }

    @Test
    fun `canUseFeature returns true for free users within weekly limit`() = runTest {
        // Given
        val subscriptionFlow = MutableStateFlow<SubscriptionState>(SubscriptionState.FREE)
        every { subscriptionService.subscriptionState } returns subscriptionFlow
        every { userPreferences.getWeeklyTranscriptionCount() } returns 2

        // When
        val result = premiumFeatureManager.canUseFeature(PremiumFeature.TRANSCRIPTION)

        // Then
        assertTrue(result)
        verify {
            analyticsService.trackFeatureAccess(
                feature = PremiumFeature.TRANSCRIPTION.name,
                isAllowed = true,
                subscriptionState = SubscriptionState.FREE
            )
        }
    }

    @Test
    fun `incrementTranscriptionCount updates count for free users`() = runTest {
        // Given
        val subscriptionFlow = MutableStateFlow<SubscriptionState>(SubscriptionState.FREE)
        every { subscriptionService.subscriptionState } returns subscriptionFlow
        every { userPreferences.getWeeklyTranscriptionCount() } returns 1

        // When
        premiumFeatureManager.incrementTranscriptionCount()

        // Then
        verify {
            userPreferences.setWeeklyTranscriptionCount(2)
            analyticsService.trackWeeklyLimitUpdate(
                newCount = 2,
                remainingCount = 1
            )
        }
    }

    @Test
    fun `incrementTranscriptionCount does not update for premium users`() = runTest {
        // Given
        val subscriptionFlow = MutableStateFlow<SubscriptionState>(SubscriptionState.MONTHLY)
        every { subscriptionService.subscriptionState } returns subscriptionFlow

        // When
        premiumFeatureManager.incrementTranscriptionCount()

        // Then
        verify(exactly = 0) {
            userPreferences.setWeeklyTranscriptionCount(any())
            analyticsService.trackWeeklyLimitUpdate(any(), any())
        }
    }

    @Test
    fun `resetWeeklyCount resets count and tracks analytics`() {
        // When
        premiumFeatureManager.resetWeeklyCount()

        // Then
        verify {
            userPreferences.setWeeklyTranscriptionCount(0)
            analyticsService.trackWeeklyLimitUpdate(
                newCount = 0,
                remainingCount = 3
            )
        }
    }

    @Test
    fun `getRemainingTranscriptions returns correct count`() {
        // Given
        every { userPreferences.getWeeklyTranscriptionCount() } returns 2

        // When
        val remaining = premiumFeatureManager.getRemainingTranscriptions()

        // Then
        assertEquals(1, remaining)
    }

    @Test
    fun `canUseFeature returns false for non-transcription features for free users`() = runTest {
        // Given
        val subscriptionFlow = MutableStateFlow<SubscriptionState>(SubscriptionState.FREE)
        every { subscriptionService.subscriptionState } returns subscriptionFlow

        // When
        val result = premiumFeatureManager.canUseFeature(PremiumFeature.CLOUD_SYNC)

        // Then
        assertFalse(result)
        verify {
            analyticsService.trackFeatureAccess(
                feature = PremiumFeature.CLOUD_SYNC.name,
                isAllowed = false,
                subscriptionState = SubscriptionState.FREE
            )
        }
    }
} 