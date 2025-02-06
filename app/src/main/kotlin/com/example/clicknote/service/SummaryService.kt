package com.example.clicknote.service

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

enum class SummaryState {
    IDLE,
    GENERATING,
    SUCCESS,
    ERROR
} 