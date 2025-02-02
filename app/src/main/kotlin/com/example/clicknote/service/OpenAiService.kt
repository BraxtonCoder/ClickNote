package com.example.clicknote.service

import java.io.File
import kotlinx.coroutines.flow.Flow
import com.example.clicknote.data.model.TranscriptionResult

interface OpenAiService {
    suspend fun transcribeAudio(audioFile: File): String
    suspend fun transcribeWithTimestamps(audioFile: File): TranscriptionResult
    suspend fun generateSummary(text: String): String
    suspend fun detectSpeakers(audioFile: File): List<String>
    suspend fun cleanup()
} 