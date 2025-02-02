package com.example.clicknote.service

import java.io.File
import kotlinx.coroutines.flow.Flow
import com.example.clicknote.domain.model.Language
import com.example.clicknote.data.model.TranscriptionResult

interface WhisperService {
    val transcriptionProgress: Flow<Float>
    val transcriptionState: Flow<TranscriptionState>
    
    suspend fun initialize()
    suspend fun transcribe(audioFile: File): String
    suspend fun transcribeWithTimestamps(audioFile: File, language: Language? = null): TranscriptionResult
    suspend fun transcribeStream(audioStream: Flow<ByteArray>): Flow<String>
    suspend fun detectSpeakers(audioFile: File): List<String>
    fun cancelTranscription()
    fun isAvailable(): Boolean
    suspend fun cleanup()
}

enum class TranscriptionState {
    IDLE,
    TRANSCRIBING,
    COMPLETED,
    CANCELLED,
    ERROR
} 