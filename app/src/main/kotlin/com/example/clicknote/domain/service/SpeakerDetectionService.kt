package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.SpeakerProfile
import java.io.File
import kotlinx.coroutines.flow.Flow

/**
 * Interface for services that can detect and identify speakers in audio
 */
interface SpeakerDetectionService : BaseService {
    /**
     * Detect the number of speakers in an audio sample
     * @param audioData Raw audio data in bytes
     * @return Number of detected speakers
     */
    suspend fun detectSpeakers(audioData: ByteArray): Result<Int>

    /**
     * Identify speakers and map them to labels
     * @param audioData Raw audio data in bytes
     * @return Map of speaker IDs to labels (e.g. "Person 1", "John")
     */
    suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>

    /**
     * Get the current speaker detection confidence
     */
    fun getSpeakerConfidence(): Flow<Float>

    /**
     * Get the list of known speaker profiles
     */
    suspend fun getKnownSpeakers(): Result<List<SpeakerProfile>>

    /**
     * Train speaker profile with audio sample
     * @param profile Speaker profile information
     * @param audioData Raw audio data in bytes
     * @return Updated speaker profile
     */
    suspend fun trainSpeakerProfile(profile: SpeakerProfile, audioData: ByteArray): Result<SpeakerProfile>

    /**
     * Detect and identify speakers in an audio file
     */
    suspend fun detectSpeakers(file: File): Result<List<String>>

    /**
     * Delete speaker profile
     */
    suspend fun deleteSpeakerProfile(speakerId: String): Result<Unit>

    /**
     * Get confidence score for speaker identification
     */
    suspend fun getSpeakerConfidence(speakerId: String, audioSample: ByteArray): Result<Float>
} 