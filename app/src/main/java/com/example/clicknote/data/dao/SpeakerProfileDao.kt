package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.SpeakerProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface SpeakerProfileDao {
    @Query("SELECT * FROM speaker_profiles WHERE is_active = 1 ORDER BY last_used DESC")
    fun getActiveSpeakers(): Flow<List<SpeakerProfile>>

    @Query("SELECT * FROM speaker_profiles WHERE id = :id")
    suspend fun getSpeakerById(id: String): SpeakerProfile?

    @Query("""
        SELECT * FROM speaker_profiles 
        WHERE name LIKE '%' || :query || '%'
        AND is_active = 1
        ORDER BY last_used DESC
    """)
    fun searchSpeakers(query: String): Flow<List<SpeakerProfile>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(speaker: SpeakerProfile)

    @Update
    suspend fun update(speaker: SpeakerProfile)

    @Query("UPDATE speaker_profiles SET is_active = 0 WHERE id = :id")
    suspend fun deactivate(id: String)

    @Query("UPDATE speaker_profiles SET last_used = :timestamp WHERE id = :id")
    suspend fun updateLastUsed(id: String, timestamp: Long = System.currentTimeMillis())

    @Query("""
        UPDATE speaker_profiles 
        SET sample_count = sample_count + 1,
            average_confidence = :newConfidence
        WHERE id = :id
    """)
    suspend fun updateConfidence(id: String, newConfidence: Float)
}