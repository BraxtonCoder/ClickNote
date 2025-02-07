package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.SpeakerProfile
import kotlinx.coroutines.flow.StateFlow

interface SpeakerProfileRepository {
    val profiles: StateFlow<List<SpeakerProfile>>

    suspend fun createProfile(name: String): Result<SpeakerProfile>
    suspend fun getProfile(id: String): Result<SpeakerProfile>
    suspend fun updateProfile(profile: SpeakerProfile): Result<Unit>
    suspend fun deleteProfile(id: String): Result<Unit>
    suspend fun getAllProfiles(): Result<List<SpeakerProfile>>
    suspend fun matchSpeaker(audioData: ByteArray): Result<SpeakerProfile?>
    suspend fun verifyProfile(profile: SpeakerProfile, audioData: ByteArray): Result<Boolean>
} 