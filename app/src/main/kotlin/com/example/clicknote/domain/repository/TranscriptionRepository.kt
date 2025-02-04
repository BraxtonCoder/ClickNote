package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.TranscriptionEvent
import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.domain.model.TranscriptionSettings
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TranscriptionRepository {
    /**
     * Transcribe audio data to text
     */
    suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String>

    /**
     * Transcribe audio file to text
     */
    suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String>

    /**
     * Generate a summary of the transcribed text
     */
    suspend fun generateSummary(text: String): Result<String>

    /**
     * Detect and identify speakers in an audio file
     */
    suspend fun detectSpeakers(file: File): Result<List<String>>

    /**
     * Get list of available languages for transcription
     */
    suspend fun getAvailableLanguages(): List<String>

    /**
     * Cancel ongoing transcription
     */
    fun cancelTranscription()

    /**
     * Clean up resources
     */
    suspend fun cleanup()

    /**
     * Save transcription result
     */
    suspend fun saveTranscription(transcriptionResult: TranscriptionResult)

    /**
     * Get all transcriptions
     */
    suspend fun getTranscriptions(): List<TranscriptionResult>

    /**
     * Get transcription by ID
     */
    suspend fun getTranscriptionById(id: String): TranscriptionResult?

    /**
     * Delete transcription
     */
    suspend fun deleteTranscription(id: String)

    /**
     * Save audio file for transcription
     */
    suspend fun saveTranscriptionAudio(id: String, audioBytes: ByteArray): String

    /**
     * Delete audio file for transcription
     */
    suspend fun deleteTranscriptionAudio(id: String)

    /**
     * Flow of transcription events
     */
    val events: Flow<TranscriptionEvent>
} 