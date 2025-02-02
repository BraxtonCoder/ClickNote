package com.example.clicknote.domain.model

/**
 * Represents the different subscription plans available in the app
 * @property price The price of the subscription in GBP
 * @property weeklyLimit The number of transcriptions allowed per week (null for unlimited)
 */
enum class SubscriptionPlan {
    FREE,
    MONTHLY,
    ANNUAL;

    val price: Float
        get() = when (this) {
            FREE -> 0f
            MONTHLY -> 9.99f
            ANNUAL -> 98f
        }

    val weeklyLimit: Int
        get() = when (this) {
            FREE -> 3
            MONTHLY, ANNUAL -> Int.MAX_VALUE
        }

    fun isPremium(): Boolean = this == MONTHLY || this == ANNUAL
} 