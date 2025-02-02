package com.example.clicknote.service.model

import com.example.clicknote.domain.model.TranscriptionLanguage

data class TranscriptionResponse(
    val text: String,
    val segments: List<TranscriptionSegment> = emptyList(),
    val language: TranscriptionLanguage = TranscriptionLanguage.ENGLISH,
    val duration: Float = 0f,
    val speakers: List<String> = emptyList()
)

data class TranscriptionSegment(
    val id: Int,
    val text: String,
    val start: Float,
    val end: Float,
    val speaker: String? = null,
    val confidence: Float = 0f
) 