package com.example.clicknote.domain.model

/**
 * Represents available subscription plans
 */
sealed class SubscriptionPlan {
    object Free : SubscriptionPlan() {
        override val name: String = "Free"
        override val price: Double = 0.0
        override val period: SubscriptionPeriod = SubscriptionPeriod.NONE
        override val transcriptionsPerWeek: Int = 3
        override val features: List<String> = listOf(
            "3 transcriptions per week",
            "Basic transcription",
            "Local storage only",
            "Standard support"
        )
    }

    data class Monthly(
        override val price: Double = 9.99
    ) : SubscriptionPlan() {
        override val name: String = "Monthly"
        override val period: SubscriptionPeriod = SubscriptionPeriod.MONTHLY
        override val transcriptionsPerWeek: Int = Int.MAX_VALUE
        override val features: List<String> = listOf(
            "Unlimited transcriptions",
            "Advanced AI transcription",
            "Cloud storage",
            "Priority support",
            "All premium features"
        )
    }

    data class Annual(
        override val price: Double = 98.0
    ) : SubscriptionPlan() {
        override val name: String = "Annual"
        override val period: SubscriptionPeriod = SubscriptionPeriod.ANNUAL
        override val transcriptionsPerWeek: Int = Int.MAX_VALUE
        override val features: List<String> = listOf(
            "Unlimited transcriptions",
            "Advanced AI transcription",
            "Cloud storage",
            "Priority support",
            "All premium features",
            "2 months free"
        )
    }

    abstract val name: String
    abstract val price: Double
    abstract val period: SubscriptionPeriod
    abstract val transcriptionsPerWeek: Int
    abstract val features: List<String>

    companion object {
        fun fromId(id: String): SubscriptionPlan = when (id) {
            Free.name -> Free
            Monthly().name -> Monthly()
            Annual().name -> Annual()
            else -> Free
        }
    }
}

enum class SubscriptionType {
    FREE,
    PREMIUM
} 