package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.CallRecordingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CallRecordingDao {
    @Query("SELECT * FROM call_recordings WHERE is_deleted = 0 ORDER BY created_at DESC")
    fun getAllCallRecordings(): Flow<List<CallRecordingEntity>>

    @Query("SELECT * FROM call_recordings WHERE id = :id")
    suspend fun getCallRecordingById(id: String): CallRecordingEntity?

    @Query("SELECT * FROM call_recordings WHERE phone_number = :phoneNumber AND is_deleted = 0 ORDER BY created_at DESC")
    fun getCallRecordingsByPhoneNumber(phoneNumber: String): Flow<List<CallRecordingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCallRecording(recording: CallRecordingEntity)

    @Update
    suspend fun updateCallRecording(recording: CallRecordingEntity)

    @Query("UPDATE call_recordings SET is_deleted = 1, deleted_at = :timestamp WHERE id = :id")
    suspend fun moveToTrash(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("DELETE FROM call_recordings WHERE id = :id")
    suspend fun deleteCallRecording(id: String)

    @Query("DELETE FROM call_recordings WHERE is_deleted = 1 AND deleted_at < :timestamp")
    suspend fun deleteOldRecordings(timestamp: Long)

    @Query("SELECT * FROM call_recordings WHERE is_deleted = 1 ORDER BY deleted_at DESC")
    fun getTrash(): Flow<List<CallRecordingEntity>>

    @Query("UPDATE call_recordings SET is_deleted = 0, deleted_at = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: String)

    @Query("UPDATE call_recordings SET sync_status = :status WHERE id = :id")
    suspend fun updateSyncStatus(id: String, status: Int)

    @Query("SELECT * FROM call_recordings WHERE sync_status = :status")
    suspend fun getRecordingsBySyncStatus(status: Int): List<CallRecordingEntity>
} 