package com.example.clicknote.domain.model

import java.time.LocalDateTime
import com.example.clicknote.data.model.SubscriptionTier

data class SubscriptionStatus(
    val tier: SubscriptionTier = SubscriptionTier.Free(),
    val subscriptionId: String? = null,
    val isActive: Boolean = false,
    val weeklyUsageCount: Int = 0,
    val subscriptionStartDate: LocalDateTime? = null,
    val subscriptionEndDate: LocalDateTime? = null,
    val isGracePeriod: Boolean = false,
    val gracePeriodEndDate: LocalDateTime? = null,
    val weeklyResetDate: LocalDateTime? = null
) {
    companion object {
        val Loading = SubscriptionStatus()
    }
}

enum class SubscriptionTier(val weeklyLimit: Int) {
    FREE(3),
    MONTHLY(Int.MAX_VALUE),
    ANNUAL(Int.MAX_VALUE)
} 