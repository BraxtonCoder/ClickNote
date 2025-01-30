package com.example.clicknote.service

import java.io.File
import kotlinx.coroutines.flow.Flow

interface TranscriptionService {
    /**
     * Stream of transcription progress from 0.0 to 1.0
     */
    val progress: Flow<Float>
    
    /**
     * Stream indicating if transcription is currently in progress
     */
    val isTranscribing: Flow<Boolean>

    /**
     * Transcribe an audio file synchronously
     */
    suspend fun transcribeFile(audioFile: File): TranscriptionResult

    /**
     * Transcribe an audio stream in real-time
     */
    suspend fun transcribeStream(audioStream: Flow<ByteArray>): Flow<TranscriptionResult>

    /**
     * Start real-time transcription from audio stream
     */
    fun startRealtimeTranscription(audioStream: Flow<ByteArray>)

    /**
     * Stop real-time transcription
     */
    fun stopRealtimeTranscription()

    /**
     * Generate a summary of the transcribed text
     */
    suspend fun generateSummary(text: String): Result<String>

    /**
     * Detect and label speakers in the audio
     */
    suspend fun detectSpeakers(audioFile: File): List<Speaker>

    /**
     * Get list of available transcription languages
     */
    fun getAvailableLanguages(): List<Language>

    /**
     * Clean up resources
     */
    suspend fun cleanup()
}

data class TranscriptionResult(
    val text: String,
    val confidence: Float = 0f,
    val timestamp: Long = System.currentTimeMillis(),
    val speaker: Speaker? = null,
    val isInterim: Boolean = false
) {
    companion object {
        fun Error(message: String) = TranscriptionResult(text = message, confidence = 0f)
    }
}

data class Speaker(
    val id: String,
    val name: String? = null,
    val confidence: Float = 0f
)

data class Language(
    val code: String,
    val name: String,
    val isSupported: Boolean = true
) 