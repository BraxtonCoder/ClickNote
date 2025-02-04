package com.example.clicknote.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class TranscriptionResult(
    val text: String,
    val confidence: Float,
    val timestamps: List<Timestamp>,
    val speakers: List<String>
)

@Serializable
data class Timestamp(
    val startTime: Long,
    val endTime: Long,
    val text: String,
    val speaker: String? = null
) 