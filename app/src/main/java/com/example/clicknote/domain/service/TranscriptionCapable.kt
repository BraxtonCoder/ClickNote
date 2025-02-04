package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.model.TranscriptionStatus
import kotlinx.coroutines.flow.Flow
import java.io.File

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
    suspend fun transcribeAudio(audioData: ByteArray, language: String? = null): String
    suspend fun transcribeFile(filePath: String, language: String? = null): String
    
    // Language detection
    suspend fun detectLanguage(audioData: ByteArray): String
    suspend fun getAvailableLanguages(): List<String>
    
    // Speaker detection
    suspend fun detectSpeakers(audioData: ByteArray): Int
    suspend fun identifySpeakers(audioData: ByteArray): List<String>
    
    // Summary generation
    suspend fun generateSummary(
        text: String,
        id: String,
        noteId: String,
        content: String,
        wordCount: Int,
        sourceWordCount: Int
    ): String
    
    // Real-time transcription control
    suspend fun startTranscription()
    suspend fun stopTranscription()
    suspend fun getTranscriptionText(): String
    suspend fun getTranscriptionStatus(): TranscriptionStatus
    
    // Audio processing
    suspend fun enhanceAudio(audioData: ByteArray): ByteArray
    
    enum class TranscriptionStatus {
        IDLE,
        RECORDING,
        TRANSCRIBING,
        COMPLETED,
        ERROR
    }
    
    data class TranscriptionResult(
        val text: String,
        val confidence: Float,
        val timestamps: List<Timestamp>,
        val speakers: List<String>
    )
    
    data class Timestamp(
        val startTime: Long,
        val endTime: Long,
        val text: String,
        val speaker: String? = null
    )
} 