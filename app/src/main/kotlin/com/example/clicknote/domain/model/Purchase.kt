package com.example.clicknote.domain.model

import java.time.LocalDateTime

data class Purchase(
    val id: String,
    val userId: String,
    val plan: SubscriptionPlan,
    val amount: Double,
    val currency: String = "GBP",
    val status: PurchaseStatus = PurchaseStatus.PENDING,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expiresAt: LocalDateTime? = null,
    val stripePaymentIntentId: String? = null,
    val stripeCustomerId: String? = null
)

enum class PurchaseStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUNDED,
    CANCELLED
} 