package com.example.clicknote.domain.model

/**
 * Data class representing a speaker profile
 */
data class SpeakerProfile(
    val id: String,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val totalDuration: Double = 0.0,
    val averageConfidence: Float = 0f,
    val verificationThreshold: Float = 0.85f,
    val isVerified: Boolean = false,
    val verificationCount: Int = 0,
    val speakerCharacteristics: Map<String, Float> = emptyMap(),
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun createEmpty(id: String = "", name: String = ""): SpeakerProfile {
            return SpeakerProfile(
                id = id,
                name = name
            )
        }
    }
} 