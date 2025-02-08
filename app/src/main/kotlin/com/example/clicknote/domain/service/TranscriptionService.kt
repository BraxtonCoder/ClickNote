package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionEvent
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.model.TranscriptionResult
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Interface for services that can detect languages.
 */
interface LanguageDetectionService : BaseService {
    suspend fun detectLanguage(audioData: ByteArray): Result<String>
    suspend fun getAvailableLanguages(): Result<List<String>>
}

/**
 * Main transcription service interface that combines all capabilities.
 */
interface TranscriptionService : TranscriptionCapable {
    override val events: Flow<TranscriptionEvent>
    
    /**
     * Generate a summary of the transcribed text
     */
    suspend fun generateSummary(text: String): Result<String>

    /**
     * Cancel ongoing transcription
     */
    fun cancelTranscription()

    fun getTranscriptionProgress(): Flow<Float>

    suspend fun transcribeAudio(audioFile: File): Result<String>
    suspend fun transcribeWithTimestamps(audioFile: File): Result<TranscriptionResult>
    suspend fun detectLanguage(audioFile: File): Result<String>
    suspend fun detectSpeakers(audioFile: File): Result<Int>
    suspend fun getProgress(): Flow<Float>
    suspend fun cancel()
    suspend fun isTranscribing(): Boolean
    suspend fun setLanguage(languageCode: String)
    suspend fun cleanup()
}

data class TranscriptionResult(
    val text: String,
    val segments: List<TranscriptionSegment> = emptyList(),
    val language: String = "en",
    val speakerCount: Int = 1,
    val confidence: Float = 0f,
    val duration: Long = 0L
)

data class TranscriptionSegment(
    val startTime: Long,
    val endTime: Long,
    val text: String,
    val speakerLabel: String? = null,
    val confidence: Float = 0f
) 