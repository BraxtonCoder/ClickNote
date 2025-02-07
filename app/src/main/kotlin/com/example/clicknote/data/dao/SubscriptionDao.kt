package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.SubscriptionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscriptions WHERE userId = :userId")
    fun getSubscription(userId: String): Flow<SubscriptionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubscription(subscription: SubscriptionEntity)

    @Update
    suspend fun updateSubscription(subscription: SubscriptionEntity)

    @Query("DELETE FROM subscriptions WHERE userId = :userId")
    suspend fun deleteSubscription(userId: String)

    @Query("UPDATE subscriptions SET remainingFreeNotes = :count WHERE userId = :userId")
    suspend fun updateRemainingFreeNotes(userId: String, count: Int)

    @Query("SELECT remainingFreeNotes FROM subscriptions WHERE userId = :userId")
    suspend fun getRemainingFreeNotes(userId: String): Int?

    @Query("UPDATE subscriptions SET isActive = :isActive, expirationDate = :expirationDate WHERE userId = :userId")
    suspend fun updateSubscriptionStatus(userId: String, isActive: Boolean, expirationDate: Long?)
} 