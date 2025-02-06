package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Summary
import com.example.clicknote.domain.model.SummaryRequest
import com.example.clicknote.domain.model.SummaryOptions
import com.example.clicknote.domain.model.TemplateType
import kotlinx.coroutines.flow.Flow

interface SummaryService {
    suspend fun generateSummary(text: String): Result<Summary>
    suspend fun generateSummaryWithTemplate(text: String, template: TemplateType): Result<Summary>
    suspend fun generateKeyPoints(text: String): Result<List<String>>
    suspend fun generateSummaryWithOptions(request: SummaryRequest): Result<Summary>
    fun observeSummaryProgress(): Flow<Float>
    suspend fun cancelSummaryGeneration()
    suspend fun isSummarizing(): Boolean
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