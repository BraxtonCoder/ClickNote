package com.example.clicknote.domain.worker

interface SummaryWorker {
    suspend fun generateSummary(text: String): Result<String>
    suspend fun generateSummaryWithTemplate(text: String, template: String): Result<String>
    suspend fun generateKeyPoints(text: String): Result<List<String>>
    fun cancelSummaryGeneration()
    suspend fun isSummarizing(): Boolean
} 