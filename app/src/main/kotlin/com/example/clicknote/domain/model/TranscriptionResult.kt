package com.example.clicknote.domain.model

data class TranscriptionResult(
    val text: String,
    val confidence: Float,
    val language: String,
    val segments: List<TranscriptionSegment> = emptyList(),
    val speakers: Map<String, String> = emptyMap(),
    val duration: Long = 0L,
    val wordCount: Int = text.split(" ").size,
    val timestamp: Long = System.currentTimeMillis()
)

data class TranscriptionSegment(
    val start: Long,
    val end: Long,
    val text: String,
    val speaker: String? = null,
    val confidence: Float = 1.0f
) 