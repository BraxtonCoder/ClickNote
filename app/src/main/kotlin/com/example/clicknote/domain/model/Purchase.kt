package com.example.clicknote.domain.model

import java.time.LocalDateTime

/**
 * Represents a purchase/subscription transaction
 */
data class Purchase(
    val id: String,
    val userId: String,
    val plan: SubscriptionPlan,
    val amount: Double,
    val currency: String = "GBP",
    val status: PurchaseStatus,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime? = null,
    val cancelledAt: LocalDateTime? = null,
    val paymentMethodId: String? = null,
    val subscriptionId: String? = null,
    val metadata: Map<String, String> = emptyMap()
)

/**
 * Represents the status of a purchase/subscription transaction
 */
enum class PurchaseStatus {
    PENDING,    // Payment is being processed
    COMPLETED,  // Payment was successful
    FAILED,     // Payment failed
    REFUNDED,   // Payment was refunded
    CANCELLED,  // Subscription was cancelled
    EXPIRED     // Subscription has expired
} 