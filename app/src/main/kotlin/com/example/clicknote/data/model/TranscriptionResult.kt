package com.example.clicknote.data.model

import com.example.clicknote.domain.model.TranscriptionLanguage

data class TranscriptionResult(
    val text: String,
    val segments: List<TranscriptionSegment> = emptyList(),
    val language: TranscriptionLanguage = TranscriptionLanguage.ENGLISH,
    val speakers: List<String> = emptyList(),
    val confidence: Float = 1.0f,
    val duration: Long = 0L
)

data class TranscriptionSegment(
    val text: String,
    val startTime: Long,
    val endTime: Long,
    val speaker: String? = null,
    val confidence: Float = 1.0f
) 