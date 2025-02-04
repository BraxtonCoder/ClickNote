package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.*
import kotlinx.coroutines.flow.Flow

interface BaseService {
    val id: String
    suspend fun cleanup()
    fun isInitialized(): Boolean
}

interface TranscriptionCapable : BaseService {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    // Core transcription operations
    suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String>
    suspend fun transcribeFile(file: String, settings: TranscriptionSettings): Result<String>
    
    // Language detection
    suspend fun detectLanguage(audioData: ByteArray): Result<String>
    suspend fun getAvailableLanguages(): Result<List<String>>
    
    // Speaker detection
    suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    suspend fun identifySpeakers(audioData: ByteArray): Result<List<String>>
    
    // Summary generation
    suspend fun generateSummary(text: String, template: SummaryTemplate?): Result<Summary>
    
    // Real-time transcription control
    suspend fun startTranscription()
    suspend fun stopTranscription()
    suspend fun getTranscriptionText(): String
    suspend fun getTranscriptionStatus(): TranscriptionStatus
    
    // Audio processing
    suspend fun enhanceAudio(audioData: ByteArray): ByteArray
    
    // Events flow for tracking transcription events
    val events: Flow<TranscriptionEvent>
    
    enum class TranscriptionStatus {
        IDLE,
        RECORDING,
        TRANSCRIBING,
        COMPLETED,
        ERROR
    }
} 