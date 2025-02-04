package com.example.clicknote.domain.model

/**
 * Represents available subscription plans
 */
sealed class SubscriptionPlan {
    abstract val id: String
    abstract val displayName: String
    abstract val price: Double
    abstract val weeklyLimit: Int
    abstract val description: String

    object Free : SubscriptionPlan() {
        override val id = "free"
        override val displayName = "Free Plan"
        override val price = 0.0
        override val weeklyLimit = 3
        override val description = "3 transcriptions per week"
    }

    object Monthly : SubscriptionPlan() {
        override val id = "monthly_subscription"
        override val displayName = "Monthly Plan"
        override val price = 9.99
        override val weeklyLimit = Int.MAX_VALUE
        override val description = "Unlimited transcriptions"
    }

    object Annual : SubscriptionPlan() {
        override val id = "annual_subscription"
        override val displayName = "Annual Plan"
        override val price = 98.0
        override val weeklyLimit = Int.MAX_VALUE
        override val description = "Unlimited transcriptions"
    }

    companion object {
        fun fromId(id: String): SubscriptionPlan = when (id) {
            Free.id -> Free
            Monthly.id -> Monthly
            Annual.id -> Annual
            else -> Free
        }
    }
}

enum class SubscriptionPeriod {
    WEEKLY,
    MONTHLY,
    ANNUAL,
    NONE
}

enum class SubscriptionType {
    FREE,
    PREMIUM
}

enum class SubscriptionPlan(
    val displayName: String,
    val price: Double,
    val weeklyLimit: Int,
    val description: String
) {
    FREE(
        displayName = "Free Plan",
        price = 0.0,
        weeklyLimit = 3,
        description = "3 transcriptions per week"
    ),
    MONTHLY(
        displayName = "Monthly Plan",
        price = 9.99,
        weeklyLimit = Int.MAX_VALUE,
        description = "Unlimited transcriptions"
    ),
    ANNUAL(
        displayName = "Annual Plan",
        price = 98.0,
        weeklyLimit = Int.MAX_VALUE,
        description = "Unlimited transcriptions"
    );

    val isPremium: Boolean
        get() = this != FREE
} 