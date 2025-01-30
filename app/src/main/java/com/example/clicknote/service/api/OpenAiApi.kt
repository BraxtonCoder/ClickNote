package com.example.clicknote.service.api

import java.io.File
import com.example.clicknote.domain.model.Language
import com.example.clicknote.data.model.TranscriptionResult
import com.example.clicknote.service.model.SummaryRequest
import com.example.clicknote.service.model.SummaryResponse
import com.example.clicknote.service.model.TranscriptionRequest
import com.example.clicknote.service.model.TranscriptionResponse

interface OpenAiApi {
    suspend fun complete(apiKey: String, prompt: String): String
    suspend fun transcribeAudio(apiKey: String, audioFile: File): String
    suspend fun transcribeAudioWithTimestamps(apiKey: String, audioFile: File, language: Language? = null): TranscriptionResult
    suspend fun detectSpeakers(apiKey: String, audioFile: File): List<String>
    suspend fun summarizeText(apiKey: String, text: String): String
    suspend fun transcribe(apiKey: String, request: TranscriptionRequest): TranscriptionResponse
    suspend fun summarize(apiKey: String, request: SummaryRequest): SummaryResponse
} 