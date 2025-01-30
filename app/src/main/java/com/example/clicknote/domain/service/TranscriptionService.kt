package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.TranscriptionResult
import kotlinx.coroutines.flow.Flow
import java.io.File

interface TranscriptionService {
    suspend fun transcribeAudio(audioPath: String): Result<TranscriptionResult>
    fun getTranscriptionProgress(): Flow<Float>
    fun cancelTranscription()
    fun isTranscribing(): Boolean
    suspend fun detectLanguage(audioPath: String): String
    suspend fun identifySpeakers(audioPath: String): List<String>

    suspend fun transcribeAudio(
        audioFile: File,
        language: String? = null,
        prompt: String? = null
    ): Result<String>
    
    suspend fun transcribeAudioStream(
        audioData: ByteArray,
        language: String? = null,
        prompt: String? = null
    ): Result<String>
} 