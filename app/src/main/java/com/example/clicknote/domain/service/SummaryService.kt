package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface SummaryService : BaseService {
    override val id: String
    override fun cleanup()
    override fun isInitialized(): Boolean
    suspend fun generateSummary(text: String): Result<Summary>
    suspend fun generateSummaryWithTemplate(text: String, template: SummaryTemplate): Result<Summary>
    fun getSummaryProgress(): Flow<Float>
    fun cancelSummaryGeneration()
    fun isSummarizing(): Boolean
    suspend fun getAvailableTemplates(): Result<List<SummaryTemplate>>
}

data class Summary(
    val text: String,
    val keyPoints: List<String>,
    val topics: List<String>,
    val sentiment: String,
    val wordCount: Int,
    val readingTime: Int
)

enum class SummaryTemplate {
    GENERAL,
    BUSINESS,
    ACADEMIC,
    TECHNICAL,
    MEETING,
    CONVERSATION
} 