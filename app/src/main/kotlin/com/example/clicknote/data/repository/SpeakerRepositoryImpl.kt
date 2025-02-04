package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.SpeakerDao
import com.example.clicknote.data.entity.Speaker as SpeakerEntity
import com.example.clicknote.domain.model.Speaker
import com.example.clicknote.domain.repository.SpeakerRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeakerRepositoryImpl @Inject constructor(
    private val speakerDao: SpeakerDao
) : SpeakerRepository {
    
    override fun getAllSpeakers(): Flow<List<Speaker>> {
        return speakerDao.getAllSpeakers().map { speakers ->
            speakers.map { it.toDomain() }
        }
    }
    
    override fun getSpeakerById(id: String): Flow<Speaker?> {
        return speakerDao.getSpeakerById(id).map { it?.toDomain() }
    }
    
    override fun getSpeakerByVoiceSignature(voiceSignature: String): Flow<Speaker?> {
        return speakerDao.getSpeakerByVoiceSignature(voiceSignature).map { it?.toDomain() }
    }
    
    override suspend fun insertSpeaker(speaker: Speaker) {
        speakerDao.insertSpeaker(speaker.toEntity())
    }
    
    override suspend fun updateSpeaker(speaker: Speaker) {
        speakerDao.updateSpeaker(speaker.toEntity())
    }
    
    override suspend fun deleteSpeaker(speaker: Speaker) {
        speakerDao.deleteSpeaker(speaker.toEntity())
    }
    
    override suspend fun updateLastUsed(speakerId: String) {
        speakerDao.updateLastUsed(speakerId, LocalDateTime.now())
    }
    
    override suspend fun updateSpeakerName(speakerId: String, name: String) {
        speakerDao.updateSpeakerName(speakerId, name)
    }
    
    override suspend fun updateSpeakerColor(speakerId: String, color: Int) {
        speakerDao.updateSpeakerColor(speakerId, color)
    }
    
    override fun searchSpeakers(query: String): Flow<List<Speaker>> {
        return speakerDao.searchSpeakers(query).map { speakers ->
            speakers.map { it.toDomain() }
        }
    }
    
    override fun getCustomNamedSpeakers(): Flow<List<Speaker>> {
        return speakerDao.getCustomNamedSpeakers().map { speakers ->
            speakers.map { it.toDomain() }
        }
    }
    
    override suspend fun getOrCreateSpeaker(voiceSignature: String): Speaker {
        return speakerDao.getOrCreateSpeaker(
            voiceSignature = voiceSignature,
            defaultName = "Speaker ${UUID.randomUUID().toString().take(4)}",
            color = generateRandomColor()
        ).toDomain()
    }

    private fun generateRandomColor(): Int {
        val colors = listOf(
            0xFF1976D2.toInt(), // Blue
            0xFF388E3C.toInt(), // Green
            0xFFF57C00.toInt(), // Orange
            0xFF7B1FA2.toInt(), // Purple
            0xFFD32F2F.toInt(), // Red
            0xFF00796B.toInt()  // Teal
        )
        return colors.random()
    }

    private fun SpeakerEntity.toDomain() = Speaker(
        id = id,
        name = name,
        confidence = 1.0f, // Default confidence for existing speakers
        startTime = lastUsed.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
        endTime = updatedAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
    )

    private fun Speaker.toEntity() = SpeakerEntity(
        id = id,
        name = name,
        voiceSignature = null, // Voice signature is managed separately
        color = generateRandomColor(),
        isCustomName = true,
        createdAt = LocalDateTime.now(),
        updatedAt = LocalDateTime.now(),
        lastUsed = LocalDateTime.now()
    )
} 