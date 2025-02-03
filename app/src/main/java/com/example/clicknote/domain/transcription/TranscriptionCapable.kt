package com.example.clicknote.domain.transcription

import kotlinx.coroutines.flow.Flow
import java.io.File

interface TranscriptionCapable {
    suspend fun startTranscription(outputFile: File? = null)
    suspend fun stopTranscription()
    suspend fun getTranscriptionText(): String
    suspend fun getTranscriptionStatus(): Flow<TranscriptionStatus>
    suspend fun summarizeTranscription(text: String): String
    suspend fun detectSpeakers(audioData: ByteArray): List<String>
    suspend fun enhanceAudio(audioData: ByteArray): ByteArray
    
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
    
    enum class TranscriptionStatus {
        IDLE,
        RECORDING,
        TRANSCRIBING,
        COMPLETED,
        ERROR
    }
} 