package com.example.clicknote.service

import java.io.File
import kotlinx.coroutines.flow.Flow

interface OnlineTranscriptionService {
    suspend fun transcribeFile(audioFile: File): TranscriptionResult
    suspend fun transcribeAudioData(audioData: ByteArray): TranscriptionResult
    suspend fun generateSummary(text: String): Result<String>
    suspend fun detectSpeakers(audioFile: File): List<Speaker>
    suspend fun cleanup()
} 