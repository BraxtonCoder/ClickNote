package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface TranscriptionCapable {
    suspend fun startTranscription()
    suspend fun stopTranscription()
    suspend fun getTranscriptionText(): String
    suspend fun getTranscriptionStatus(): Flow<TranscriptionStatus>
    suspend fun summarizeTranscription(text: String): String
    suspend fun detectSpeakers(audioData: ByteArray): List<String>
    suspend fun enhanceAudio(audioData: ByteArray): ByteArray
    
    enum class TranscriptionStatus {
        IDLE,
        RECORDING,
        TRANSCRIBING,
        COMPLETED,
        ERROR
    }
} 