package com.example.clicknote.data.local

import androidx.room.*
import com.example.clicknote.data.model.SubscriptionStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription_status LIMIT 1")
    suspend fun getSubscriptionStatus(): SubscriptionStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSubscriptionStatus(status: SubscriptionStatus)

    @Query("DELETE FROM subscription_status")
    suspend fun clearSubscriptionStatus()

    @Query("UPDATE subscription_status SET remaining_free_notes = :count WHERE userId = :userId")
    suspend fun updateRemainingFreeNotes(userId: String, count: Int)

    @Query("SELECT remaining_free_notes FROM subscription_status WHERE userId = :userId")
    suspend fun getRemainingFreeNotes(userId: String): Int?

    @Query("SELECT * FROM subscription_status WHERE userId = :userId")
    fun observeSubscriptionStatus(userId: String): Flow<SubscriptionStatus?>
} 