package com.example.clicknote.data.model

sealed class SubscriptionTier {
    abstract val displayName: String
    abstract val monthlyPrice: Double
    abstract val yearlyPrice: Double
    abstract val weeklyLimit: Int
    abstract val features: List<String>

    data class Free(
        override val displayName: String = "Free",
        override val monthlyPrice: Double = 0.0,
        override val yearlyPrice: Double = 0.0,
        override val weeklyLimit: Int = 3,
        override val features: List<String> = listOf(
            "3 transcriptions per week",
            "Basic transcription",
            "Local storage only",
            "Standard support"
        )
    ) : SubscriptionTier()

    data class Monthly(
        override val displayName: String = "Premium Monthly",
        override val monthlyPrice: Double = 9.99,
        override val yearlyPrice: Double = 119.88,
        override val weeklyLimit: Int = Int.MAX_VALUE,
        override val features: List<String> = listOf(
            "Unlimited transcriptions",
            "Advanced AI transcription",
            "Cloud storage",
            "Priority support",
            "Multiple device sync",
            "Advanced AI features",
            "Call transcription"
        )
    ) : SubscriptionTier()

    data class Annual(
        override val displayName: String = "Premium Yearly",
        override val monthlyPrice: Double = 8.17,
        override val yearlyPrice: Double = 98.0,
        override val weeklyLimit: Int = Int.MAX_VALUE,
        override val features: List<String> = listOf(
            "Unlimited transcriptions",
            "Advanced AI transcription",
            "Cloud storage",
            "Priority support",
            "Multiple device sync",
            "Advanced AI features",
            "Call transcription",
            "18% discount"
        )
    ) : SubscriptionTier()

    companion object {
        fun fromName(name: String): SubscriptionTier = when (name.lowercase()) {
            "free" -> Free()
            "monthly" -> Monthly()
            "annual" -> Annual()
            else -> throw IllegalArgumentException("Unknown subscription tier: $name")
        }
    }
} 