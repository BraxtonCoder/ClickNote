package com.example.clicknote.service.model

import com.example.clicknote.domain.model.EntityType
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.model.TranscriptionSegment
import java.io.File

data class TranscriptionRequest(
    val audioFile: File,
    val language: TranscriptionLanguage = TranscriptionLanguage.ENGLISH,
    val prompt: String? = null,
    val temperature: Float = 0f,
    val detectSpeakers: Boolean = false
)

data class TranscriptionResponse(
    val text: String,
    val segments: List<TranscriptionSegment> = emptyList(),
    val language: TranscriptionLanguage = TranscriptionLanguage.ENGLISH
)

data class SummaryRequest(
    val text: String,
    val maxLength: Int? = null,
    val template: String? = null,
    val language: TranscriptionLanguage = TranscriptionLanguage.ENGLISH,
    val extractKeyPoints: Boolean = true,
    val extractEntities: Boolean = true,
    val entityTypes: Set<EntityType> = setOf(
        EntityType.PERSON,
        EntityType.ORGANIZATION,
        EntityType.LOCATION,
        EntityType.DATE,
        EntityType.TIME
    )
)

data class SummaryResponse(
    val summary: String,
    val keyPoints: List<String> = emptyList(),
    val topics: List<String> = emptyList(),
    val entities: List<String> = emptyList(),
    val timeline: List<String> = emptyList()
)

data class ChatMessage(
    val role: ChatRole,
    val content: String
)

enum class ChatRole {
    SYSTEM,
    USER,
    ASSISTANT
}

data class TranscriptionSegment(
    val text: String,
    val startTime: Long, // milliseconds
    val endTime: Long, // milliseconds
    val speaker: String? = null,
    val confidence: Float = 0f
) 