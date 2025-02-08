package com.example.clicknote.domain.service

import java.io.File
import kotlinx.coroutines.flow.Flow

interface OpenAiService {
    suspend fun transcribeAudio(audioFile: File, language: String): String
    suspend fun generateSummary(text: String): String
    suspend fun generateKeyPoints(text: String): List<String>
    suspend fun detectLanguage(audioFile: File): String
    suspend fun detectSpeakers(audioFile: File): Int
    suspend fun improveAudioQuality(audioFile: File): File
    suspend fun generateTags(text: String): Result<List<String>>
    suspend fun extractKeyPoints(text: String): Result<List<String>>
    suspend fun categorizeContent(text: String): Result<List<String>>
    suspend fun analyzeEmotion(text: String): Result<EmotionAnalysis>
    suspend fun generateTitle(text: String): Result<String>
    suspend fun enhanceText(text: String, style: TextStyle): Result<String>
    fun getProgress(): Flow<Float>
    fun cancelOperation()
    suspend fun isOperationInProgress(): Boolean
    suspend fun getAvailableModels(): List<OpenAiModel>
}

data class SummaryOptions(
    val maxLength: Int = 500,
    val style: TextStyle = TextStyle.CONCISE,
    val format: OutputFormat = OutputFormat.PARAGRAPH,
    val language: String = "en"
)

enum class TextStyle {
    CONCISE,
    DETAILED,
    TECHNICAL,
    CASUAL,
    PROFESSIONAL,
    CREATIVE
}

enum class OutputFormat {
    PARAGRAPH,
    BULLET_POINTS,
    NUMBERED_LIST,
    OUTLINE,
    MARKDOWN
}

data class EmotionAnalysis(
    val primaryEmotion: String,
    val confidence: Float,
    val emotions: Map<String, Float>
)

data class OpenAiModel(
    val id: String,
    val name: String,
    val capabilities: List<String>,
    val maxTokens: Int,
    val isAvailable: Boolean
) 