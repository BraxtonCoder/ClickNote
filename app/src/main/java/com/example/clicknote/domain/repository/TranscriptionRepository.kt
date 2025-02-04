package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.model.TranscriptionEvent
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TranscriptionRepository {
    // Event stream
    val events: Flow<TranscriptionEvent>

    // Core transcription operations
    suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings
    ): Result<String>

    suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings
    ): Result<String>

    // Additional features
    suspend fun generateSummary(text: String): Result<String>
    suspend fun detectSpeakers(file: File): Result<List<String>>
    suspend fun getAvailableLanguages(): List<String>

    // Control operations
    fun cancelTranscription()
    suspend fun cleanup()

    // Storage operations
    suspend fun saveTranscription(transcriptionResult: TranscriptionResult)
    suspend fun getTranscriptions(): List<TranscriptionResult>
    suspend fun getTranscriptionById(id: String): TranscriptionResult
    suspend fun deleteTranscription(id: String)
    suspend fun saveTranscriptionAudio(id: String, audioBytes: ByteArray): String
    suspend fun deleteTranscriptionAudio(id: String)
} 