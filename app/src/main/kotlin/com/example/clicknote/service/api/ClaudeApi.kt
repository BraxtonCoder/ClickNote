package com.example.clicknote.service.api

import com.example.clicknote.service.model.SummaryRequest
import com.example.clicknote.service.model.SummaryResponse
import com.example.clicknote.service.model.TranscriptionRequest
import com.example.clicknote.service.model.TranscriptionResponse

interface ClaudeApi {
    suspend fun transcribe(
        apiKey: String,
        request: TranscriptionRequest
    ): TranscriptionResponse

    suspend fun summarize(
        apiKey: String,
        request: SummaryRequest
    ): SummaryResponse

    suspend fun complete(
        apiKey: String,
        prompt: String
    ): String

    suspend fun summarize(text: String, apiKey: String): String
    suspend fun analyze(text: String, apiKey: String): AnalysisResult
    suspend fun extractKeyPoints(text: String, apiKey: String): List<KeyPoint>
    suspend fun categorize(text: String, apiKey: String): List<Category>
}

data class AnalysisResult(
    val summary: String,
    val sentiment: String,
    val topics: List<String>,
    val keyPoints: List<KeyPoint>,
    val categories: List<Category>
)

data class KeyPoint(
    val text: String,
    val confidence: Float,
    val category: String? = null
)

data class Category(
    val name: String,
    val confidence: Float,
    val subcategories: List<String> = emptyList()
) 