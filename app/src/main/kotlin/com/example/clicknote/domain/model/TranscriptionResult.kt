package com.example.clicknote.domain.model

data class TranscriptionResult(
    val text: String,
    val language: String? = null,
    val confidence: Float = 0f,
    val segments: List<TranscriptionSegment> = emptyList(),
    val speakers: Map<String, String> = emptyMap(),
    val duration: Long = 0L,
    val wordCount: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

data class TranscriptionSegment(
    val start: Long,
    val end: Long,
    val text: String,
    val speaker: String?,
    val confidence: Float
) 