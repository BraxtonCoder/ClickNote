package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo
import com.example.clicknote.domain.model.SubscriptionPlan
import com.example.clicknote.domain.model.SubscriptionStatus
import java.time.LocalDateTime

@Entity(tableName = "subscription_status")
data class SubscriptionStatusEntity(
    @PrimaryKey
    val userId: String,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean,

    @ColumnInfo(name = "plan_type")
    val planType: String,

    @ColumnInfo(name = "expiration_date")
    val expirationDate: Long?,

    @ColumnInfo(name = "is_auto_renewing")
    val isAutoRenewing: Boolean,

    @ColumnInfo(name = "remaining_free_notes")
    val remainingFreeNotes: Int,

    @ColumnInfo(name = "last_reset_date")
    val lastResetDate: Long,

    @ColumnInfo(name = "is_grace_period")
    val isGracePeriod: Boolean = false,

    @ColumnInfo(name = "grace_period_end_date")
    val gracePeriodEndDate: Long? = null,

    @ColumnInfo(name = "error_message")
    val errorMessage: String? = null,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    fun toDomainModel(): SubscriptionStatus {
        return when {
            errorMessage != null -> SubscriptionStatus.Error(errorMessage)
            isGracePeriod -> SubscriptionStatus.GracePeriod(
                expirationDate = gracePeriodEndDate ?: System.currentTimeMillis(),
                plan = planTypeToDomainPlan(planType)
            )
            isActive && expirationDate != null -> SubscriptionStatus.Premium(
                expirationDate = expirationDate,
                isAutoRenewing = isAutoRenewing,
                plan = planTypeToDomainPlan(planType)
            )
            else -> SubscriptionStatus.Free
        }
    }

    private fun planTypeToDomainPlan(type: String): SubscriptionPlan {
        return when (type.uppercase()) {
            "MONTHLY" -> SubscriptionPlan.Monthly()
            "ANNUAL" -> SubscriptionPlan.Annual()
            else -> SubscriptionPlan.Free
        }
    }

    companion object {
        fun fromDomainModel(userId: String, status: SubscriptionStatus): SubscriptionStatusEntity {
            return when (status) {
                is SubscriptionStatus.Premium -> SubscriptionStatusEntity(
                    userId = userId,
                    isActive = true,
                    planType = status.plan.name,
                    expirationDate = status.expirationDate,
                    isAutoRenewing = status.isAutoRenewing,
                    remainingFreeNotes = Int.MAX_VALUE,
                    lastResetDate = System.currentTimeMillis()
                )
                is SubscriptionStatus.GracePeriod -> SubscriptionStatusEntity(
                    userId = userId,
                    isActive = true,
                    planType = status.plan.name,
                    expirationDate = null,
                    isAutoRenewing = false,
                    remainingFreeNotes = 0,
                    lastResetDate = System.currentTimeMillis(),
                    isGracePeriod = true,
                    gracePeriodEndDate = status.expirationDate
                )
                is SubscriptionStatus.Error -> SubscriptionStatusEntity(
                    userId = userId,
                    isActive = false,
                    planType = "FREE",
                    expirationDate = null,
                    isAutoRenewing = false,
                    remainingFreeNotes = 0,
                    lastResetDate = System.currentTimeMillis(),
                    errorMessage = status.message
                )
                else -> SubscriptionStatusEntity(
                    userId = userId,
                    isActive = false,
                    planType = "FREE",
                    expirationDate = null,
                    isAutoRenewing = false,
                    remainingFreeNotes = 3,
                    lastResetDate = System.currentTimeMillis()
                )
            }
        }
    }
} 