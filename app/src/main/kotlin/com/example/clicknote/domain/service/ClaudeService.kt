package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface ClaudeService {
    suspend fun generateSummary(text: String, options: ClaudeSummaryOptions = ClaudeSummaryOptions()): Result<String>
    suspend fun analyzeContent(text: String): Result<ContentAnalysis>
    suspend fun generateInsights(text: String): Result<List<String>>
    suspend fun structureContent(text: String, format: ContentFormat): Result<String>
    suspend fun generateQuestions(text: String): Result<List<String>>
    suspend fun extractActionItems(text: String): Result<List<String>>
    fun getProgress(): Flow<Float>
    fun cancelOperation()
    suspend fun isOperationInProgress(): Boolean
    suspend fun getAvailableModels(): List<ClaudeModel>
}

data class ClaudeSummaryOptions(
    val maxLength: Int = 500,
    val style: ClaudeStyle = ClaudeStyle.BALANCED,
    val format: ContentFormat = ContentFormat.PARAGRAPH,
    val language: String = "en",
    val includeTags: Boolean = false
)

enum class ClaudeStyle {
    BALANCED,
    ACADEMIC,
    CREATIVE,
    PROFESSIONAL,
    CONVERSATIONAL
}

enum class ContentFormat {
    PARAGRAPH,
    BULLET_POINTS,
    OUTLINE,
    MARKDOWN,
    JSON
}

data class ContentAnalysis(
    val topics: List<String>,
    val sentiment: String,
    val complexity: Float,
    val keyTerms: List<String>,
    val structure: ContentStructure
)

data class ContentStructure(
    val sections: List<String>,
    val hierarchy: Map<String, List<String>>,
    val wordCount: Int,
    val readingTime: Int
)

data class ClaudeModel(
    val id: String,
    val name: String,
    val capabilities: List<String>,
    val contextWindow: Int,
    val isAvailable: Boolean
) 