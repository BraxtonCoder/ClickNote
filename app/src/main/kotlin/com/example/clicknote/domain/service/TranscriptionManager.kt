package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Note
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TranscriptionManager {
    suspend fun startTranscription(audioFile: File): Result<String>
    suspend fun stopTranscription()
    suspend fun pauseTranscription()
    suspend fun resumeTranscription()
    suspend fun getTranscriptionProgress(): Flow<Float>
    suspend fun getTranscriptionStatus(): Flow<TranscriptionStatus>
    suspend fun saveTranscription(note: Note): Result<Unit>
    suspend fun isTranscribing(): Boolean
    suspend fun setLanguage(languageCode: String)
    suspend fun detectLanguage(audioFile: File): Result<String>
    suspend fun detectSpeakers(audioFile: File): Result<Int>
    suspend fun generateSummary(text: String): Result<String>
    suspend fun cleanup()
}

enum class TranscriptionStatus {
    IDLE,
    PREPARING,
    TRANSCRIBING,
    PAUSED,
    COMPLETED,
    ERROR
} 