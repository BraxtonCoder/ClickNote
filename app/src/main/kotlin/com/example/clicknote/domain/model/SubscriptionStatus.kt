package com.example.clicknote.domain.model

import java.time.LocalDateTime
import com.example.clicknote.domain.model.SubscriptionTier
import com.example.clicknote.domain.model.SubscriptionPlan

/**
 * Represents the current status of a user's subscription
 */
sealed class SubscriptionStatus {
    object Free : SubscriptionStatus()
    object Trial : SubscriptionStatus()
    data class Premium(
        val expirationDate: Long,
        val isAutoRenewing: Boolean,
        val plan: SubscriptionPlan
    ) : SubscriptionStatus()
    data class GracePeriod(
        val expirationDate: Long,
        val plan: SubscriptionPlan
    ) : SubscriptionStatus()
    object Expired : SubscriptionStatus()
    object Cancelled : SubscriptionStatus()
    data class Error(val message: String) : SubscriptionStatus()
    object Pending : SubscriptionStatus()

    companion object {
        fun fromString(status: String): SubscriptionStatus {
            return when (status) {
                "Free" -> Free
                "Cancelled" -> Cancelled
                else -> Error("Invalid status: $status")
            }
        }

        fun valueOf(status: String): SubscriptionStatus {
            return fromString(status)
        }

        fun toString(status: SubscriptionStatus): String = when (status) {
            is Free -> "FREE"
            is Trial -> "TRIAL"
            is Premium -> "PREMIUM"
            is GracePeriod -> "GRACE_PERIOD"
            is Expired -> "EXPIRED"
            is Cancelled -> "CANCELLED"
            is Error -> "ERROR"
            is Pending -> "PENDING"
        }
    }

    override fun toString(): String {
        return when (this) {
            is Free -> "Free"
            is Premium -> "Premium"
            is GracePeriod -> "GracePeriod"
            is Error -> "Error"
            is Cancelled -> "Cancelled"
            is Trial -> "Trial"
            is Expired -> "Expired"
            is Pending -> "Pending"
        }
    }
} 