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
} 