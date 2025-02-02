package com.example.clicknote.data.dao

import androidx.room.*
import com.example.clicknote.data.entity.Speaker
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

@Dao
interface SpeakerDao {
    @Query("SELECT * FROM speakers ORDER BY lastUsed DESC")
    fun getAllSpeakers(): Flow<List<Speaker>>

    @Query("SELECT * FROM speakers WHERE id = :id")
    suspend fun getSpeakerById(id: String): Speaker?

    @Query("SELECT * FROM speakers WHERE voiceSignature = :voiceSignature")
    suspend fun getSpeakerByVoiceSignature(voiceSignature: String): Speaker?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeaker(speaker: Speaker)

    @Update
    suspend fun updateSpeaker(speaker: Speaker)

    @Delete
    suspend fun deleteSpeaker(speaker: Speaker)

    @Query("UPDATE speakers SET lastUsed = :timestamp WHERE id = :speakerId")
    suspend fun updateLastUsed(speakerId: String, timestamp: LocalDateTime = LocalDateTime.now())

    @Query("UPDATE speakers SET name = :newName, isCustomName = true, updatedAt = :timestamp WHERE id = :speakerId")
    suspend fun updateSpeakerName(
        speakerId: String,
        newName: String,
        timestamp: LocalDateTime = LocalDateTime.now()
    )

    @Query("UPDATE speakers SET color = :newColor, updatedAt = :timestamp WHERE id = :speakerId")
    suspend fun updateSpeakerColor(
        speakerId: String,
        newColor: Int,
        timestamp: LocalDateTime = LocalDateTime.now()
    )

    @Query("""
        SELECT * FROM speakers 
        WHERE name LIKE '%' || :query || '%' 
        ORDER BY lastUsed DESC
    """)
    fun searchSpeakers(query: String): Flow<List<Speaker>>

    @Query("SELECT * FROM speakers WHERE isCustomName = 1 ORDER BY name ASC")
    fun getCustomNamedSpeakers(): Flow<List<Speaker>>

    @Transaction
    suspend fun getOrCreateSpeaker(voiceSignature: String, defaultName: String, color: Int): Speaker {
        val existingSpeaker = getSpeakerByVoiceSignature(voiceSignature)
        if (existingSpeaker != null) {
            updateLastUsed(existingSpeaker.id)
            return existingSpeaker
        }

        val newSpeaker = Speaker(
            id = java.util.UUID.randomUUID().toString(),
            name = defaultName,
            voiceSignature = voiceSignature,
            color = color
        )
        insertSpeaker(newSpeaker)
        return newSpeaker
    }
} 