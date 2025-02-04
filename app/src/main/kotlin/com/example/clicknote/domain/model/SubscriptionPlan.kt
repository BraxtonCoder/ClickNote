package com.example.clicknote.domain.model

/**
 * Represents the different subscription plans available in the app
 * @property price The price of the subscription in GBP
 * @property weeklyLimit The number of transcriptions allowed per week (null for unlimited)
 */
data class SubscriptionPlan(
    val id: String,
    val name: String,
    val description: String,
    val price: String,
    val period: SubscriptionPeriod,
    val productId: String,
    val weeklyTranscriptionLimit: Int = Int.MAX_VALUE
) {
    companion object {
        val FREE = SubscriptionPlan(
            id = "free",
            name = "Free Plan",
            description = "3 transcriptions per week",
            price = "Free",
            period = SubscriptionPeriod.NONE,
            productId = "",
            weeklyTranscriptionLimit = 3
        )

        val MONTHLY = SubscriptionPlan(
            id = "monthly",
            name = "Monthly Plan",
            description = "Unlimited transcriptions",
            price = "£9.99/month",
            period = SubscriptionPeriod.MONTHLY,
            productId = "monthly_subscription"
        )

        val ANNUAL = SubscriptionPlan(
            id = "annual",
            name = "Annual Plan",
            description = "Unlimited transcriptions",
            price = "£98/year",
            period = SubscriptionPeriod.ANNUAL,
            productId = "annual_subscription"
        )
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