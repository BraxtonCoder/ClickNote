package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.SpeakerDao
import com.example.clicknote.domain.model.Speaker
import com.example.clicknote.domain.repository.SpeakerRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeakerRepositoryImpl @Inject constructor(
    private val speakerDao: SpeakerDao
) : SpeakerRepository {
    
    override fun getAllSpeakers(): Flow<List<Speaker>> {
        return speakerDao.getAllSpeakers()
    }
    
    override fun getSpeakerById(id: String): Flow<Speaker?> {
        return speakerDao.getSpeakerById(id)
    }
    
    override fun getSpeakerByVoiceSignature(voiceSignature: String): Flow<Speaker?> {
        return speakerDao.getSpeakerByVoiceSignature(voiceSignature)
    }
    
    override suspend fun insertSpeaker(speaker: Speaker) {
        speakerDao.insertSpeaker(speaker)
    }
    
    override suspend fun updateSpeaker(speaker: Speaker) {
        speakerDao.updateSpeaker(speaker)
    }
    
    override suspend fun deleteSpeaker(speaker: Speaker) {
        speakerDao.deleteSpeaker(speaker)
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
        return speakerDao.searchSpeakers(query)
    }
    
    override fun getCustomNamedSpeakers(): Flow<List<Speaker>> {
        return speakerDao.getCustomNamedSpeakers()
    }
    
    override suspend fun getOrCreateSpeaker(voiceSignature: String): Speaker {
        return speakerDao.getOrCreateSpeaker(voiceSignature)
    }
} 