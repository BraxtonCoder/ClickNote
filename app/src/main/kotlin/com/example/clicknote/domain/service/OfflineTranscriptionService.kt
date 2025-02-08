package com.example.clicknote.domain.service

import java.io.File
import kotlinx.coroutines.flow.Flow

interface OfflineTranscriptionService {
    suspend fun transcribeAudio(audioFile: File): Result<String>
    suspend fun transcribeWithTimestamps(audioFile: File): Result<TranscriptionResult>
    suspend fun detectLanguage(audioFile: File): Result<String>
    suspend fun detectSpeakers(audioFile: File): Result<Int>
    suspend fun getProgress(): Flow<Float>
    suspend fun cancel()
    suspend fun isTranscribing(): Boolean
    suspend fun setLanguage(languageCode: String)
    suspend fun cleanup()
    suspend fun isModelDownloaded(language: String): Boolean
    suspend fun downloadModel(language: String): Result<Unit>
    suspend fun deleteModel(language: String): Result<Unit>
    suspend fun getDownloadedModels(): List<String>
    suspend fun getModelSize(language: String): Long
} 