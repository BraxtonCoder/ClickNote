package com.example.clicknote.domain.service

import java.io.File

interface SpeakerDetectionService {
    /**
     * Detect and identify speakers in an audio file
     */
    suspend fun detectSpeakers(file: File): Result<List<String>>

    /**
     * Train speaker profile with audio sample
     */
    suspend fun trainSpeakerProfile(speakerId: String, audioSample: ByteArray): Result<Unit>

    /**
     * Delete speaker profile
     */
    suspend fun deleteSpeakerProfile(speakerId: String): Result<Unit>

    /**
     * Get confidence score for speaker identification
     */
    suspend fun getSpeakerConfidence(speakerId: String, audioSample: ByteArray): Result<Float>

    /**
     * Clean up resources
     */
    suspend fun cleanup()
} 