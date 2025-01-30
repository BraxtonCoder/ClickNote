package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface SummaryService {
    suspend fun generateSummary(text: String): Result<Summary>
    suspend fun generateSummaryWithTemplate(text: String, template: SummaryTemplate): Result<Summary>
    fun getSummaryProgress(): Flow<Float>
    fun cancelSummaryGeneration()
    fun isSummarizing(): Boolean
    suspend fun getAvailableTemplates(): List<SummaryTemplate>
}

data class Summary(
    val text: String,
    val keyPoints: List<String>,
    val topics: List<String>,
    val sentiment: String,
    val wordCount: Int,
    val readingTime: Int
)

data class SummaryTemplate(
    val id: String,
    val name: String,
    val description: String,
    val prompt: String,
    val category: String
) 