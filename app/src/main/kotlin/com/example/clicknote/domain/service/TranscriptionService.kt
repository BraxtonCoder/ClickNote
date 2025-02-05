package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionEvent
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.model.TranscriptionResult
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Base interface for all services in the application.
 */
interface BaseService {
    val id: String
    suspend fun cleanup()
    fun isInitialized(): Boolean
}

/**
 * Interface for services that can perform transcription operations.
 */
interface TranscriptionCapable : BaseService {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    // Core transcription operations
    suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<TranscriptionResult>
    suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<TranscriptionResult>
    
    // Language detection
    suspend fun detectLanguage(audioData: ByteArray): Result<String>
    suspend fun getAvailableLanguages(): Result<List<String>>
    
    // Speaker detection
    suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>
    
    // Summary generation
    suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>
    
    // Events flow for tracking transcription events
    val events: Flow<TranscriptionEvent>
}

/**
 * Interface for services that can detect languages.
 */
interface LanguageDetectionService : BaseService {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    suspend fun detectLanguage(audioData: ByteArray): Result<String>
    suspend fun getAvailableLanguages(): Result<List<String>>
}

/**
 * Interface for services that can detect and identify speakers.
 */
interface SpeakerDetectionService : BaseService {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>
}

/**
 * Interface for services that can generate summaries.
 */
interface SummaryService : BaseService {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>
}

/**
 * Marker interface for services that can operate online.
 */
interface OnlineCapableService : TranscriptionCapable

/**
 * Marker interface for services that can operate offline.
 */
interface OfflineCapableService : TranscriptionCapable

/**
 * Main transcription service interface that combines all capabilities.
 */
interface TranscriptionService : TranscriptionCapable {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    override val events: Flow<TranscriptionEvent>
    
    override suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<TranscriptionResult>
    override suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<TranscriptionResult>
    override suspend fun detectLanguage(audioData: ByteArray): Result<String>
    override suspend fun getAvailableLanguages(): Result<List<String>>
    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    override suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>
    override suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>

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