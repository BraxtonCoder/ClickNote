package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.*
import kotlinx.coroutines.flow.Flow
import java.io.File

interface WhisperTranscriptionService : TranscriptionCapable {
    override val events: Flow<TranscriptionEvent>
    
    override suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings
    ): Result<TranscriptionResult>
    
    override suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings
    ): Result<TranscriptionResult>
    
    override suspend fun detectLanguage(audioData: ByteArray): Result<String>
    override suspend fun getAvailableLanguages(): Result<List<String>>
    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    override suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>
    override suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>
} 