package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TranscriptionCapable {
    val id: String
    val events: Flow<TranscriptionEvent>
    
    fun isInitialized(): Boolean
    
    suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings
    ): Result<TranscriptionResult>
    
    suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings
    ): Result<TranscriptionResult>
    
    suspend fun detectLanguage(audioData: ByteArray): Result<String>
    suspend fun getAvailableLanguages(): Result<List<String>>
    suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>
    suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>
} 