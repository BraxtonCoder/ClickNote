package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryRequest
import com.example.clicknote.domain.model.SummaryOptions
import com.example.clicknote.domain.model.TemplateType
import com.example.clicknote.domain.model.SummaryTemplate
import kotlinx.coroutines.flow.Flow

/**
 * Interface for services that can generate summaries from text content
 */
interface SummaryService : BaseService {
    /**
     * Generate a summary using a specific template
     * @param text The text to summarize
     * @param template The template to use for summarization
     * @return The generated summary
     */
    suspend fun generateSummary(text: String, template: SummaryTemplate): Result<Summary>

    /**
     * Generate a quick summary without a template
     * @param text The text to summarize
     * @return The generated summary text
     */
    suspend fun generateQuickSummary(text: String): Result<String>

    /**
     * Get available summary templates
     * @return List of available templates
     */
    suspend fun getAvailableTemplates(): Result<List<SummaryTemplate>>

    /**
     * Get the current summarization progress
     */
    fun getSummarizationProgress(): Flow<Float>

    /**
     * Cancel ongoing summarization
     */
    suspend fun cancelSummarization()

    /**
     * Check if summarization is in progress
     */
    suspend fun isSummarizing(): Boolean

    suspend fun generateKeyPoints(text: String): Result<List<String>>
    suspend fun generateSummaryWithOptions(request: SummaryRequest): Result<Summary>
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