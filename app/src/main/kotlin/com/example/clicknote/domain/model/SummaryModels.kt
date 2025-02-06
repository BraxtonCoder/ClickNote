package com.example.clicknote.domain.model

import com.example.clicknote.domain.model.TranscriptionLanguage

data class SummaryRequest(
    val text: String,
    val options: SummaryOptions = SummaryOptions()
)

data class SummaryResponse(
    val summary: String,
    val keyPoints: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val entities: Map<EntityType, List<String>> = emptyMap(),
    val timeline: List<TimelineEvent> = emptyList(),
    val speakers: List<String> = emptyList()
)

data class TimelineEvent(
    val timestamp: String,
    val description: String
)

data class Summary(
    val id: String,
    val noteId: String,
    val content: String,
    val keyPoints: List<String> = emptyList(),
    val actionItems: List<String> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val entities: List<Entity> = emptyList(),
    val timeline: List<TimelineEvent> = emptyList(),
    val sentiment: Sentiment? = null,
    val wordCount: Int,
    val sourceWordCount: Int,
    val compressionRatio: Float = wordCount.toFloat() / sourceWordCount
)

data class DetailedSummary(
    val id: String,
    val noteId: String,
    val summary: Summary,
    val topics: List<Topic>,
    val entities: List<Entity>,
    val timeline: List<TimelineEvent>
)

data class Topic(
    val id: String,
    val name: String,
    val relevance: Float,
    val mentions: Int,
    val subtopics: List<String> = emptyList()
)

data class Entity(
    val name: String,
    val type: EntityType,
    val mentions: Int = 1
)

data class SummaryOptions(
    val template: SummaryTemplate? = null,
    val maxLength: Int = 500,
    val language: TranscriptionLanguage = TranscriptionLanguage.ENGLISH,
    val format: SummaryFormat = SummaryFormat.BULLET_POINTS,
    val style: SummaryStyle = SummaryStyle.CONCISE,
    val templateType: TemplateType = TemplateType.GENERAL,
    val includeKeyPoints: Boolean = true,
    val includeActionItems: Boolean = true,
    val includeTimeline: Boolean = false,
    val includeTopics: Boolean = true,
    val includeEntities: Boolean = true,
    val includeSentiment: Boolean = false,
    val detectSpeakers: Boolean = false,
    val extractEntities: Boolean = true,
    val entityTypes: Set<EntityType> = setOf(
        EntityType.PERSON,
        EntityType.ORGANIZATION,
        EntityType.LOCATION,
        EntityType.DATE,
        EntityType.TIME
    )
)

enum class TemplateType {
    GENERAL,
    MEETING_MINUTES,
    LECTURE_NOTES,
    INTERVIEW,
    RESEARCH,
    BRAINSTORM,
    ANALYSIS,
    CUSTOM
}

enum class SummaryStyle {
    CONCISE,
    DETAILED,
    TECHNICAL,
    SIMPLIFIED,
    NARRATIVE,
    CASUAL,
    BUSINESS
}

enum class SummaryFormat {
    BULLET_POINTS,
    PARAGRAPHS,
    OUTLINE,
    TIMELINE,
    KEY_VALUE_PAIRS
}

enum class EntityType {
    PERSON,
    ORGANIZATION,
    LOCATION,
    DATE,
    TIME,
    CONCEPT,
    TECHNOLOGY,
    PRODUCT,
    EVENT,
    OTHER
}

data class Sentiment(
    val score: Float, // -1.0 to 1.0
    val label: SentimentLabel
)

enum class SentimentLabel {
    VERY_NEGATIVE,
    NEGATIVE,
    NEUTRAL,
    POSITIVE,
    VERY_POSITIVE
}

/**
 * Types of named entities that can be recognized in text
 */
enum class NamedEntityType {
    PERSON,         // Names of people
    ORGANIZATION,   // Names of organizations
    LOCATION,       // Place names
    DATE,          // Date references
    TIME,          // Time references
    EVENT,         // Event names
    TOPIC,         // Topic or subject matter
    KEYWORD        // Important keywords
} 