package com.example.clicknote.service.model

import com.example.clicknote.domain.model.EntityType
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.model.TranscriptionSegment
import java.io.File

data class TranscriptionRequest(
    val audioFile: File,
    val language: TranscriptionLanguage? = null,
    val detectSpeakers: Boolean = false,
    val useTimestamps: Boolean = true
)

data class TranscriptionResponse(
    val text: String,
    val segments: List<TranscriptionSegment> = emptyList(),
    val speakers: List<String> = emptyList()
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