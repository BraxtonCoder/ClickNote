package com.example.clicknote.service.model

data class SummaryResponse(
    val summary: String,
    val keyPoints: List<String> = emptyList(),
    val entities: List<Entity> = emptyList(),
    val topics: List<Topic> = emptyList(),
    val template: SummaryTemplate? = null
)

data class Entity(
    val name: String,
    val type: String,
    val mentions: List<Mention> = emptyList()
)

data class Mention(
    val text: String,
    val startIndex: Int,
    val endIndex: Int,
    val confidence: Float = 0f
)

data class Topic(
    val name: String,
    val relevance: Float = 0f,
    val subtopics: List<String> = emptyList()
) 