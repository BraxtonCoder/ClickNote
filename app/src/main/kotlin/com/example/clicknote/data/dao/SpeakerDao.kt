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
    fun getSpeakerById(id: String): Flow<Speaker?>

    @Query("SELECT * FROM speakers WHERE voiceSignature = :voiceSignature")
    fun getSpeakerByVoiceSignature(voiceSignature: String): Flow<Speaker?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSpeaker(speaker: Speaker)

    @Update
    suspend fun updateSpeaker(speaker: Speaker)

    @Delete
    suspend fun deleteSpeaker(speaker: Speaker)

    @Query("UPDATE speakers SET lastUsed = :lastUsed WHERE id = :speakerId")
    suspend fun updateLastUsed(speakerId: String, lastUsed: LocalDateTime)

    @Query("UPDATE speakers SET name = :name WHERE id = :speakerId")
    suspend fun updateSpeakerName(speakerId: String, name: String)

    @Query("UPDATE speakers SET color = :color WHERE id = :speakerId")
    suspend fun updateSpeakerColor(speakerId: String, color: Int)

    @Query("SELECT * FROM speakers WHERE name LIKE '%' || :query || '%'")
    fun searchSpeakers(query: String): Flow<List<Speaker>>

    @Query("SELECT * FROM speakers WHERE isCustomName = 1")
    fun getCustomNamedSpeakers(): Flow<List<Speaker>>

    @Query("SELECT * FROM speakers WHERE voiceSignature = :voiceSignature LIMIT 1")
    suspend fun findSpeakerByVoiceSignature(voiceSignature: String): Speaker?
} 