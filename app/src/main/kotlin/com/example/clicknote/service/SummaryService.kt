package com.example.clicknote.service

import com.example.clicknote.domain.model.*
import kotlinx.coroutines.flow.Flow

interface SummaryService {
    val summaryState: Flow<SummaryState>
    val progress: Flow<Float>
    val templates: Flow<List<SummaryTemplate>>

    suspend fun generateSummary(
        text: String,
        options: SummaryOptions = SummaryOptions()
    ): Result<Summary>

    suspend fun generateDetailedSummary(
        text: String,
        options: SummaryOptions = SummaryOptions()
    ): Result<DetailedSummary>

    fun getAvailableTemplates(): List<SummaryTemplate>
    fun getTemplateByName(name: String): SummaryTemplate?
    suspend fun generateStreamingSummary(text: String, options: SummaryOptions): Flow<String>
    suspend fun cancelSummarization()
    suspend fun getTemplatesForCategory(category: String): List<SummaryTemplate>
    suspend fun createTemplate(template: SummaryTemplate)
    suspend fun deleteTemplate(templateId: String)
    fun streamSummary(text: String, templateType: TemplateType? = null): Flow<String>
    suspend fun askQuestion(text: String, question: String): Result<String>
    fun isAvailable(): Boolean

    suspend fun extractTopics(
        text: String,
        maxTopics: Int = 5
    ): Result<List<Topic>>

    suspend fun extractEntities(
        text: String,
        types: Set<EntityType> = EntityType.values().toSet()
    ): Result<List<Entity>>

    suspend fun extractTimeline(
        text: String,
        maxEvents: Int = 10
    ): Result<List<TimelineEvent>>

    suspend fun generateKeyPoints(
        text: String,
        maxPoints: Int = 5
    ): Result<List<String>>

    suspend fun generateActionItems(
        text: String,
        maxItems: Int = 5
    ): Result<List<String>>

    suspend fun generateTitle(
        text: String,
        maxLength: Int = 50
    ): Result<String>

    suspend fun generateTags(
        text: String,
        maxTags: Int = 5
    ): Result<List<String>>
}

enum class SummaryState {
    IDLE,
    GENERATING,
    SUCCESS,
    ERROR
} 