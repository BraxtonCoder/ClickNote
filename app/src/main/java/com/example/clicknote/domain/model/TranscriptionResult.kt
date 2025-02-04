package com.example.clicknote.domain.model

import kotlinx.serialization.Serializable
import java.time.LocalDateTime

@Serializable
data class TranscriptionResult(
    val id: String,
    val text: String,
    val audioUrl: String? = null,
    val duration: Long,
    val isLongForm: Boolean,
    val speakers: List<String> = emptyList(),
    val summary: String? = null,
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val folderId: String? = null
) 