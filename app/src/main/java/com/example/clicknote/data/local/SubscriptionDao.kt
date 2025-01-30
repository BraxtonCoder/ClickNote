package com.example.clicknote.data.local

import androidx.room.*
import com.example.clicknote.data.model.SubscriptionStatus

@Dao
interface SubscriptionDao {
    @Query("SELECT * FROM subscription_status LIMIT 1")
    suspend fun getSubscriptionStatus(): SubscriptionStatus?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSubscriptionStatus(status: SubscriptionStatus)

    @Query("DELETE FROM subscription_status")
    suspend fun clearSubscriptionStatus()
} 