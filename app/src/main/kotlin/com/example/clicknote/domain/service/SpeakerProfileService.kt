package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow
import java.io.File

interface SpeakerProfileService {
    suspend fun createProfile(name: String, audioSamples: List<File>): Result<SpeakerProfile>
    suspend fun updateProfile(profileId: String, audioSamples: List<File>): Result<SpeakerProfile>
    suspend fun deleteProfile(profileId: String): Result<Unit>
    suspend fun getProfile(profileId: String): Result<SpeakerProfile>
    suspend fun getAllProfiles(): Result<List<SpeakerProfile>>
    suspend fun identifySpeaker(audioFile: File): Result<SpeakerMatch>
    suspend fun identifySpeakers(audioFile: File): Result<List<SpeakerSegment>>
    fun getTrainingProgress(): Flow<Float>
    fun getIdentificationProgress(): Flow<Float>
    fun cancelOperation()
    suspend fun isOperationInProgress(): Boolean
    suspend fun exportProfile(profileId: String): Result<File>
    suspend fun importProfile(file: File): Result<SpeakerProfile>
}

data class SpeakerProfile(
    val id: String,
    val name: String,
    val createdAt: Long,
    val updatedAt: Long,
    val sampleCount: Int,
    val totalDuration: Long,
    val confidence: Float,
    val characteristics: VoiceCharacteristics
)

data class VoiceCharacteristics(
    val pitch: Float,
    val tempo: Float,
    val timbre: String,
    val frequency: Map<String, Float>,
    val uniqueFeatures: List<String>
)

data class SpeakerMatch(
    val profileId: String,
    val confidence: Float,
    val characteristics: VoiceCharacteristics
)

data class SpeakerSegment(
    val startTime: Long,
    val endTime: Long,
    val speakerProfile: SpeakerProfile?,
    val confidence: Float
) 