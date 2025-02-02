package com.example.clicknote.service.model

data class SummaryRequest(
    val text: String,
    val maxLength: Int? = null,
    val extractKeyPoints: Boolean = false,
    val extractEntities: Boolean = false,
    val entityTypes: List<String> = emptyList(),
    val options: SummaryOptions = SummaryOptions()
)

data class SummaryOptions(
    val template: SummaryTemplate? = null,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 4000
)

enum class SummaryTemplate {
    BULLET_POINTS,
    PARAGRAPHS,
    KEY_POINTS,
    TIMELINE,
    TOPICS
} 