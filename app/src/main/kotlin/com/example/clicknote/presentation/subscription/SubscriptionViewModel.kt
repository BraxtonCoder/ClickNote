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
import org.json.JSONObject

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
                subscriptionRepository.currentPlan
                    .collect { plan ->
                        _state.update { 
                            it.copy(
                                isLoading = false,
                                currentPlan = plan,
                                isSubscribed = plan != SubscriptionPlan.Free
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

    fun subscribe(plan: SubscriptionPlan) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            
            try {
                subscriptionRepository.updateSubscriptionState(plan)
                analytics.track("Subscription Changed", JSONObject().apply {
                    put("plan", plan.javaClass.simpleName)
                })
            } catch (e: Exception) {
                _state.update { 
                    it.copy(error = e.message ?: "Failed to update subscription")
                }
            } finally {
                _state.update { it.copy(isLoading = false) }
            }
        }
    }

    private fun trackScreenView() {
        analytics.track("Screen View", JSONObject().apply {
            put("screen", "Subscription")
        })
    }

    override fun onCleared() {
        super.onCleared()
        analytics.track("Screen Exit", JSONObject().apply {
            put("screen_name", "Subscription")
            put("duration_seconds", (System.currentTimeMillis() - screenStartTime) / 1000)
        })
    }

    private val screenStartTime = System.currentTimeMillis()
}

data class SubscriptionState(
    val isLoading: Boolean = false,
    val isSubscribed: Boolean = false,
    val currentPlan: SubscriptionPlan = SubscriptionPlan.Free,
    val error: String? = null
) 