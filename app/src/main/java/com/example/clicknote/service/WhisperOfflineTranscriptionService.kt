package com.example.clicknote.service

import java.io.File
import kotlinx.coroutines.flow.Flow
import com.example.clicknote.domain.model.TranscriptionResult

interface WhisperOfflineTranscriptionService {
    suspend fun transcribeFile(audioFile: File): TranscriptionResult
    suspend fun transcribeAudioData(audioData: ByteArray): TranscriptionResult
    suspend fun detectSpeakers(audioFile: File): List<Speaker>
    fun getAvailableLanguages(): List<Language>
    suspend fun cleanup()
} 