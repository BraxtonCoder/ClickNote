package com.example.clicknote.domain.repository

import com.example.clicknote.domain.model.TranscriptionEvent
import com.example.clicknote.domain.model.TranscriptionSettings
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
} 