package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionSettings
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
    
    suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String>
    suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String>
    suspend fun detectLanguage(audioData: ByteArray): Result<String>
    suspend fun getAvailableLanguages(): Result<List<String>>
    suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>
    suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>
}

interface LanguageDetectionService : BaseService {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    suspend fun detectLanguage(audioData: ByteArray): Result<String>
    suspend fun getAvailableLanguages(): Result<List<String>>
}

interface SpeakerDetectionService : BaseService {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>
}

interface SummaryService : BaseService {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>
}

interface OnlineCapableService : TranscriptionCapable

interface OfflineCapableService : TranscriptionCapable