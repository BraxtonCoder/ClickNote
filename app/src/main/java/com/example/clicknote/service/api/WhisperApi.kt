package com.example.clicknote.service.api

import java.io.File

interface WhisperApi {
    suspend fun transcribe(
        apiKey: String,
        audioFile: File,
        language: String? = null,
        prompt: String? = null
    ): String

    suspend fun transcribeWithTimestamps(
        apiKey: String,
        audioFile: File,
        language: String? = null
    ): List<TranscriptionSegment>

    suspend fun detectSpeakers(
        apiKey: String,
        audioFile: File
    ): List<String>

    fun isAvailable(): Boolean
}

data class TranscriptionSegment(
    val text: String,
    val startTime: Double,
    val endTime: Double,
    val speaker: String? = null
) 