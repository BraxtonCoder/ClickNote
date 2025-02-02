package com.example.clicknote.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.data.model.SubscriptionStatus
import com.example.clicknote.data.model.SubscriptionTier
import com.example.clicknote.data.repository.SubscriptionRepository
import com.example.clicknote.service.NotificationService
import com.example.clicknote.ui.subscription.SubscriptionPlan
import com.stripe.android.paymentsheet.PaymentSheetResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _clientSecret = MutableStateFlow<String?>(null)
    val clientSecret: StateFlow<String?> = _clientSecret.asStateFlow()

    private val _showPaymentSheet = MutableStateFlow(false)
    val showPaymentSheet: StateFlow<Boolean> = _showPaymentSheet.asStateFlow()

    val subscriptionStatus: StateFlow<SubscriptionStatus> = subscriptionRepository
        .observeSubscriptionStatus()
        .onEach { status ->
            // Check usage limits for free plan
            if (status.tier == SubscriptionTier.FREE) {
                checkUsageLimits(status.weeklyUsageCount, SubscriptionTier.FREE.weeklyLimit)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SubscriptionStatus(tier = SubscriptionTier.FREE)
        )

    private var currentSubscriptionType: String? = null

    init {
        // Check if weekly usage needs to be reset
        viewModelScope.launch {
            val status = subscriptionRepository.getSubscriptionStatus()
            if (status.weeklyResetDate?.isBefore(java.time.LocalDateTime.now()) == true) {
                subscriptionRepository.resetWeeklyUsage()
                notificationService.clearUsageNotifications()
            }
        }
    }

    private fun checkUsageLimits(usedCount: Int, totalLimit: Int) {
        val usagePercentage = (usedCount.toFloat() / totalLimit) * 100

        when {
            usedCount >= totalLimit -> {
                notificationService.showUsageLimitReachedNotification()
            }
            usagePercentage >= 75 -> {
                notificationService.showUsageWarningNotification(usedCount, totalLimit)
            }
        }
    }

    fun subscribeToPlan(plan: SubscriptionPlan) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                when (plan) {
                    is SubscriptionPlan.Free -> {
                        subscriptionRepository.switchToFreePlan()
                        notificationService.clearUsageNotifications()
                    }
                    is SubscriptionPlan.Monthly -> {
                        currentSubscriptionType = "monthly"
                        val secret = subscriptionRepository.createPaymentIntent("monthly")
                        _clientSecret.value = secret
                        _showPaymentSheet.value = true
                    }
                    is SubscriptionPlan.Annual -> {
                        currentSubscriptionType = "annual"
                        val secret = subscriptionRepository.createPaymentIntent("annual")
                        _clientSecret.value = secret
                        _showPaymentSheet.value = true
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred while processing your subscription"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun handlePaymentResult(result: PaymentSheetResult) {
        when (result) {
            is PaymentSheetResult.Completed -> {
                // Payment successful, create subscription
                currentSubscriptionType?.let { subscriptionType ->
                    viewModelScope.launch {
                        try {
                            _isLoading.value = true
                            subscriptionRepository.createSubscription(
                                paymentMethodId = result.paymentIntent.id,
                                subscriptionType = subscriptionType
                            )
                            // Clear any usage notifications when upgrading
                            notificationService.clearUsageNotifications()
                            // Reset states
                            _showPaymentSheet.value = false
                            _clientSecret.value = null
                            currentSubscriptionType = null
                        } catch (e: Exception) {
                            _error.value = e.message ?: "Failed to create subscription"
                        } finally {
                            _isLoading.value = false
                        }
                    }
                }
            }
            is PaymentSheetResult.Canceled -> {
                _error.value = "Payment cancelled"
                _showPaymentSheet.value = false
                _clientSecret.value = null
            }
            is PaymentSheetResult.Failed -> {
                _error.value = result.error.message ?: "Payment failed"
                _showPaymentSheet.value = false
                _clientSecret.value = null
            }
        }
    }

    fun cancelCurrentSubscription() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                subscriptionRepository.cancelSubscription()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to cancel subscription"
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun canMakeTranscription(): Boolean {
        return subscriptionRepository.canMakeTranscription()
    }

    suspend fun incrementUsageCount() {
        subscriptionRepository.incrementUsageCount()
    }

    fun dismissError() {
        _error.value = null
    }

    fun dismissPaymentSheet() {
        _showPaymentSheet.value = false
        _clientSecret.value = null
        currentSubscriptionType = null
    }
} 
