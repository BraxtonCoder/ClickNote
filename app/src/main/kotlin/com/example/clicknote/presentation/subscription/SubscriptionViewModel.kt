package com.example.clicknote.presentation.subscription

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.repository.SubscriptionRepository
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val analytics: MixpanelAPI
) : ViewModel() {

    private val _state = MutableStateFlow(SubscriptionState())
    val state = _state.asStateFlow()

    init {
        loadCurrentSubscription()
        trackScreenView()
    }

    private fun loadCurrentSubscription() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            try {
                subscriptionRepository.getCurrentPlan()
                    .collect { plan ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                currentPlan = plan,
                                isSubscribed = plan != null && plan.id != "free"
                            )
                        }
                    }
            } catch (e: Exception) {
                _state.update { 
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load subscription status"
                    )
                }
            }
        }
    }

    fun subscribe(planId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                analytics.track("Subscription Started", mapOf(
                    "plan_id" to planId,
                    "plan_type" to if (planId == "monthly") "Monthly" else "Annual"
                ))

                val result = subscriptionRepository.purchaseSubscription(planId)
                if (result.isSuccess) {
                    analytics.track("Subscription Completed", mapOf(
                        "plan_id" to planId,
                        "plan_type" to if (planId == "monthly") "Monthly" else "Annual"
                    ))
                    loadCurrentSubscription()
                } else {
                    val error = result.exceptionOrNull()?.message ?: "Failed to process subscription"
                    _state.update { it.copy(isLoading = false, error = error) }
                    analytics.track("Subscription Failed", mapOf(
                        "plan_id" to planId,
                        "error" to error
                    ))
                }
            } catch (e: Exception) {
                val error = e.message ?: "Failed to process subscription"
                _state.update { it.copy(isLoading = false, error = error) }
                analytics.track("Subscription Failed", mapOf(
                    "plan_id" to planId,
                    "error" to error
                ))
            }
        }
    }

    private fun trackScreenView() {
        analytics.track("Screen View", mapOf(
            "screen_name" to "Subscription",
            "is_subscribed" to state.value.isSubscribed
        ))
    }

    override fun onCleared() {
        super.onCleared()
        analytics.track("Screen Exit", mapOf(
            "screen_name" to "Subscription",
            "duration_seconds" to ((System.currentTimeMillis() - screenStartTime) / 1000)
        ))
    }

    private val screenStartTime = System.currentTimeMillis()
}

data class SubscriptionState(
    val isLoading: Boolean = false,
    val isSubscribed: Boolean = false,
    val currentPlan: SubscriptionPlan? = null,
    val error: String? = null
) 