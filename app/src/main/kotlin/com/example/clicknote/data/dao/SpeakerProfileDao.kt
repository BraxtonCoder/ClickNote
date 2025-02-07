package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.SpeakerProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeakerProfileDao {
    @Query("SELECT * FROM speaker_profiles")
    fun getAllProfiles(): List<SpeakerProfileEntity>

    @Query("SELECT * FROM speaker_profiles WHERE id = :id")
    suspend fun getSpeakerById(id: String): SpeakerProfileEntity?

    @Query("SELECT * FROM speaker_profiles WHERE is_active = 1")
    fun getActiveSpeakers(): Flow<List<SpeakerProfileEntity>>

    @Query("""
        SELECT * FROM speaker_profiles 
        WHERE name LIKE '%' || :query || '%' 
        ORDER BY last_updated DESC
    """)
    fun searchSpeakers(query: String): Flow<List<SpeakerProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(profile: SpeakerProfileEntity)

    @Update
    suspend fun update(profile: SpeakerProfileEntity)

    @Query("DELETE FROM speaker_profiles WHERE id = :id")
    suspend fun delete(id: String)

    @Query("UPDATE speaker_profiles SET is_active = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("UPDATE speaker_profiles SET last_updated = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long = System.currentTimeMillis())
}