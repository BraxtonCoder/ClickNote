package com.example.clicknote.ui.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.data.model.SubscriptionTier
import com.example.clicknote.data.model.SubscriptionStatus
import com.example.clicknote.service.SubscriptionService
import com.example.clicknote.service.AnalyticsService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SubscriptionUiState(
    val currentTier: SubscriptionTier = SubscriptionTier.FREE,
    val subscriptionStatus: SubscriptionStatus = SubscriptionStatus.Loading,
    val usageCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionService: SubscriptionService,
    private val analyticsService: AnalyticsService
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionUiState())
    val uiState: StateFlow<SubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadSubscriptionStatus()
        observeUsageCount()
    }

    private fun loadSubscriptionStatus() {
        viewModelScope.launch {
            try {
                val tier = subscriptionService.getCurrentSubscription()
                _uiState.update { it.copy(
                    currentTier = tier,
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message,
                    isLoading = false
                )}
                analyticsService.trackError(e.message ?: "Failed to load subscription", "SubscriptionScreen")
            }
        }
    }

    private fun observeUsageCount() {
        viewModelScope.launch {
            subscriptionService.usageCount.collect { count ->
                _uiState.update { it.copy(usageCount = count) }
            }
        }
    }

    fun subscribe(plan: SubscriptionPlan, paymentMethodId: String) {
        viewModelScope.launch {
            try {
                val tier = when (plan) {
                    SubscriptionPlan.Free -> SubscriptionTier.Free()
                    SubscriptionPlan.Monthly -> SubscriptionTier.Monthly()
                    SubscriptionPlan.Annual -> SubscriptionTier.Annual()
                }
                subscriptionService.subscribe(tier, paymentMethodId)
                // Update subscription status after successful subscription
                updateSubscriptionStatus()
            } catch (e: Exception) {
                // Handle error
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                )}
                analyticsService.trackError(e.message ?: "Failed to subscribe", "SubscriptionScreen")
            }
        }
    }

    fun createPaymentIntent(tier: SubscriptionTier) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val amount = when (tier) {
                    SubscriptionTier.MONTHLY -> 999 // £9.99
                    SubscriptionTier.ANNUAL -> 9800 // £98.00
                    else -> 0
                }
                
                val paymentIntentId = subscriptionService.createPaymentIntent(amount)
                // Handle payment intent creation success
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                )}
                analyticsService.trackError(e.message ?: "Failed to create payment", "SubscriptionScreen")
            }
        }
    }

    fun confirmPayment(paymentIntentId: String, paymentMethodId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                subscriptionService.confirmPayment(paymentIntentId, paymentMethodId)
                    .onSuccess {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                    .onFailure { e ->
                        _uiState.update { it.copy(
                            isLoading = false,
                            error = e.message
                        )}
                        analyticsService.trackError(e.message ?: "Failed to confirm payment", "SubscriptionScreen")
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message
                )}
                analyticsService.trackError(e.message ?: "Failed to confirm payment", "SubscriptionScreen")
            }
        }
    }

    fun dismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun updateSubscriptionStatus() {
        loadSubscriptionStatus()
    }
} 