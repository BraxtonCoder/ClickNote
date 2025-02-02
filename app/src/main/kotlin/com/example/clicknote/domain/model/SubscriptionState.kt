package com.example.clicknote.domain.model

sealed class SubscriptionState {
    object Free : SubscriptionState()
    data class Premium(
        val expirationDate: Long,
        val isAutoRenewing: Boolean,
        val plan: SubscriptionPlan
    ) : SubscriptionState()
    object Loading : SubscriptionState()
    data class Error(val message: String) : SubscriptionState()
} 