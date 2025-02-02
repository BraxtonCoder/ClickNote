package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryTemplate
import com.example.clicknote.domain.model.TranscriptionSettings
import java.io.File

interface OnlineCapableService : TranscriptionCapable {
    override val id: String
    override suspend fun cleanup()
    override fun isInitialized(): Boolean
    
    override suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String>
    override suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String>
    override suspend fun detectLanguage(audioData: ByteArray): Result<String>
    override suspend fun getAvailableLanguages(): Result<List<String>>
    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int>
    override suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>>
    override suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>
}