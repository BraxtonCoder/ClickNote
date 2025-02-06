package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionEvent
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.domain.model.TranscriptionResult
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * Interface for services that can perform transcription operations.
 */
interface TranscriptionCapable : BaseService {
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