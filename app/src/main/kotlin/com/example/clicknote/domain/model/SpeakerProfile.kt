package com.example.clicknote.domain.model

/**
 * Data class representing a speaker profile
 */
data class SpeakerProfile(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUsed: Long = System.currentTimeMillis(),
    val confidence: Float = 0f
) 