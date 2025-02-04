package com.example.clicknote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import java.time.LocalDateTime

@Entity(tableName = "subscription_status")
data class SubscriptionStatus(
    @PrimaryKey
    val userId: String,

    @ColumnInfo(name = "subscription_type")
    val subscriptionType: SubscriptionType,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "start_date")
    val startDate: LocalDateTime,

    @ColumnInfo(name = "end_date")
    val endDate: LocalDateTime,

    @ColumnInfo(name = "remaining_free_notes")
    val remainingFreeNotes: Int = 3,

    @ColumnInfo(name = "last_reset_date")
    val lastResetDate: LocalDateTime = LocalDateTime.now(),

    @ColumnInfo(name = "stripe_customer_id")
    val stripeCustomerId: String? = null,

    @ColumnInfo(name = "stripe_subscription_id")
    val stripeSubscriptionId: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun createFree(userId: String) = SubscriptionStatus(
            userId = userId,
            subscriptionType = SubscriptionType.FREE,
            isActive = true,
            startDate = LocalDateTime.now(),
            endDate = LocalDateTime.now().plusYears(100), // Effectively unlimited for free tier
            remainingFreeNotes = 3,
            lastResetDate = LocalDateTime.now()
        )
    }
}

enum class SubscriptionType {
    FREE,
    MONTHLY,
    ANNUAL
} 