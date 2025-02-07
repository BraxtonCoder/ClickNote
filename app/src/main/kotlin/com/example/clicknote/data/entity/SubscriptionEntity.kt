package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "subscriptions")
data class SubscriptionEntity(
    @PrimaryKey
    val userId: String,
    val planType: String,
    val isActive: Boolean,
    val expirationDate: Long?,
    val isAutoRenewing: Boolean,
    val remainingFreeNotes: Int,
    val lastResetDate: Long,
    val isGracePeriod: Boolean = false,
    val gracePeriodEndDate: Long? = null,
    val errorMessage: String? = null,
    val updatedAt: Long = System.currentTimeMillis()
) 