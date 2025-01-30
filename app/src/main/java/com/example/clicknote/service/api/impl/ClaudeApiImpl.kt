package com.example.clicknote.service.api.impl

import com.example.clicknote.service.api.ClaudeApi
import com.example.clicknote.service.model.SummaryRequest
import com.example.clicknote.service.model.SummaryResponse
import com.example.clicknote.service.model.TranscriptionRequest
import com.example.clicknote.service.model.TranscriptionResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClaudeApiImpl @Inject constructor() : ClaudeApi {
    override suspend fun transcribe(
        apiKey: String,
        request: TranscriptionRequest
    ): TranscriptionResponse {
        // TODO: Implement actual API call to Claude
        return TranscriptionResponse(
            text = "Transcription placeholder",
            segments = emptyList()
        )
    }

    override suspend fun summarize(
        apiKey: String,
        request: SummaryRequest
    ): SummaryResponse {
        // TODO: Implement actual API call to Claude
        return SummaryResponse(
            summary = "Summary placeholder",
            keyPoints = emptyList(),
            topics = emptyList(),
            entities = emptyList(),
            timeline = emptyList()
        )
    }

    override suspend fun complete(
        apiKey: String,
        prompt: String
    ): String {
        // TODO: Implement actual API call to Claude
        return "Completion placeholder"
    }
} 