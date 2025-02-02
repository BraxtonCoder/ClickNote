package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionSettings
import java.io.File

interface CombinedService : BaseOnlineService {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String>
    suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String>
    suspend fun detectLanguage(audioData: ByteArray): Result<String>
    suspend fun getAvailableLanguages(): Result<List<String>>
    suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>
    suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>
} 