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

data class TranscriptionMetadata(
    val language: String,
    val model: String,
    val duration: Long,
    val wordCount: Int,
    val speakerCount: Int,
    val confidence: Float,
    val processingTime: Long,
    val timestamp: Long = System.currentTimeMillis()
) 