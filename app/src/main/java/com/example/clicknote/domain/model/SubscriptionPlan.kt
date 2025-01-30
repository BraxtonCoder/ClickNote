package com.example.clicknote.domain.model

sealed class SubscriptionPlan {
    abstract val id: String
    abstract val name: String
    abstract val price: Double
    abstract val interval: String
    abstract val features: List<String>

    data class Free(
        override val id: String = "free",
        override val name: String = "Free",
        override val price: Double = 0.0,
        override val interval: String = "month",
        override val features: List<String> = listOf(
            "3 transcriptions per week",
            "Basic transcription",
            "Local storage only"
        )
    ) : SubscriptionPlan()

    data class Monthly(
        override val id: String = "price_monthly",
        override val name: String = "Premium Monthly",
        override val price: Double = 9.99,
        override val interval: String = "month",
        override val features: List<String> = listOf(
            "Unlimited transcriptions",
            "Advanced AI transcription",
            "Cloud storage",
            "Priority support"
        )
    ) : SubscriptionPlan()

    data class Annual(
        override val id: String = "price_annual",
        override val name: String = "Premium Annual",
        override val price: Double = 98.0,
        override val interval: String = "year",
        override val features: List<String> = listOf(
            "Unlimited transcriptions",
            "Advanced AI transcription",
            "Cloud storage",
            "Priority support",
            "18% discount"
        )
    ) : SubscriptionPlan()
}

enum class SubscriptionStatus {
    ACTIVE,
    EXPIRED,
    CANCELLED,
    FREE_TRIAL,
    NONE
} 