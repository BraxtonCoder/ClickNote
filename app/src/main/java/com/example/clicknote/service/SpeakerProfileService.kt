package com.example.clicknote.service

import com.example.clicknote.domain.model.SpeakerProfile
import kotlinx.coroutines.flow.Flow

interface SpeakerProfileService {
    fun getAllProfiles(): Flow<List<SpeakerProfile>>
    suspend fun createProfile(name: String): SpeakerProfile
    suspend fun updateProfile(profile: SpeakerProfile)
    suspend fun deleteProfile(profileId: String)
    suspend fun getProfileById(profileId: String): SpeakerProfile?
    suspend fun matchSpeaker(audioData: ByteArray): String?
    suspend fun cleanup()
} 