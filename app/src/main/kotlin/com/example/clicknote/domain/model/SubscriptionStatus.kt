package com.example.clicknote.domain.model

import java.time.LocalDateTime
import com.example.clicknote.domain.model.SubscriptionTier
import com.example.clicknote.domain.model.SubscriptionPlan

enum class SubscriptionStatus {
    NONE,
    FREE,
    MONTHLY,
    ANNUAL,
    GRACE_PERIOD,
    EXPIRED
}

sealed class SubscriptionStatus {
    object Free : SubscriptionStatus()
    data class Premium(
        val expirationDate: Long,
        val isAutoRenewing: Boolean,
        val plan: SubscriptionPlan
    ) : SubscriptionStatus()
    object Loading : SubscriptionStatus()
    data class Error(val message: String) : SubscriptionStatus()
} 