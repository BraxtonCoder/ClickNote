package com.example.clicknote.service.transcription

import kotlinx.coroutines.flow.Flow
import java.io.File

interface TranscriptionService {
    suspend fun transcribeAudio(audioFile: File, mode: TranscriptionMode = TranscriptionMode.ONLINE): Flow<TranscriptionResult>
    suspend fun detectSpeakers(audioFile: File): List<Speaker>
    suspend fun generateSummary(transcription: String): String
    fun isOfflineModelAvailable(): Boolean
    suspend fun downloadOfflineModel()
    suspend fun deleteOfflineModel()
}

enum class TranscriptionMode {
    ONLINE,
    OFFLINE
}

data class TranscriptionResult(
    val text: String,
    val confidence: Float,
    val timestamp: Long,
    val speaker: Speaker? = null,
    val isInterim: Boolean = false
)

data class Speaker(
    val id: String,
    val name: String? = null,
    val confidence: Float
) 