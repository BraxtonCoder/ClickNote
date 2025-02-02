package com.example.clicknote.service.api

import kotlinx.coroutines.flow.Flow
import java.io.File
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.data.model.TranscriptionResult

interface OpenAiService {
    suspend fun initialize()
    suspend fun transcribe(audioFile: File): String
    suspend fun transcribeWithTimestamps(audioFile: File, language: TranscriptionLanguage? = null): TranscriptionResult
    suspend fun transcribeStream(audioStream: Flow<ByteArray>): Flow<String>
    suspend fun detectSpeakers(audioFile: File): List<String>
    suspend fun summarize(text: String): String
    suspend fun generateSummary(text: String): Result<String>
    fun streamSummary(text: String, template: String? = null): Flow<String>
    suspend fun askQuestion(text: String, question: String): Result<String>
    suspend fun extractKeyPoints(text: String): List<String>
    fun isAvailable(): Boolean
    suspend fun cleanup()
} 