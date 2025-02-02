package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.Speaker
import kotlinx.coroutines.flow.Flow

interface SpeakerRepository {
    fun getAllSpeakers(): Flow<List<Speaker>>
    
    fun getSpeakerById(id: String): Flow<Speaker?>
    
    fun getSpeakerByVoiceSignature(voiceSignature: String): Flow<Speaker?>
    
    suspend fun insertSpeaker(speaker: Speaker)
    
    suspend fun updateSpeaker(speaker: Speaker)
    
    suspend fun deleteSpeaker(speaker: Speaker)
    
    suspend fun updateLastUsed(speakerId: String)
    
    suspend fun updateSpeakerName(speakerId: String, name: String)
    
    suspend fun updateSpeakerColor(speakerId: String, color: Int)
    
    fun searchSpeakers(query: String): Flow<List<Speaker>>
    
    fun getCustomNamedSpeakers(): Flow<List<Speaker>>
    
    suspend fun getOrCreateSpeaker(voiceSignature: String): Speaker
} 