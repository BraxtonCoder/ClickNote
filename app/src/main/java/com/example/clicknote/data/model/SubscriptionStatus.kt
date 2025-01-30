package com.example.clicknote.data.model

import java.time.LocalDateTime

data class SubscriptionStatus(
    val tier: SubscriptionTier,
    val weeklyUsageCount: Int = 0,
    val weeklyResetDate: LocalDateTime? = null,
    val subscriptionId: String? = null,
    val subscriptionEndDate: LocalDateTime? = null,
    val isActive: Boolean = true
)

enum class SubscriptionTier(
    val displayName: String,
    val weeklyLimit: Int = -1,
    val monthlyPrice: Double = 0.0
) {
    FREE("Free Plan", weeklyLimit = 3),
    MONTHLY("Monthly Plan", monthlyPrice = 9.99),
    ANNUAL("Annual Plan", monthlyPrice = 8.17) // £98/12
} 