package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.domain.model.SpeakerProfile

@Entity(tableName = "speaker_profiles")
@TypeConverters(RoomConverters::class)
data class SpeakerProfileEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val createdAt: Long,
    val lastUpdated: Long,
    val totalDuration: Double,
    val averageConfidence: Float,
    val verificationThreshold: Float,
    val isVerified: Boolean,
    val verificationCount: Int,
    val speakerCharacteristics: Map<String, Float>,
    val metadata: Map<String, String>
) {
    fun toDomain(): SpeakerProfile = SpeakerProfile(
        id = id,
        name = name,
        createdAt = createdAt,
        lastUpdated = lastUpdated,
        totalDuration = totalDuration,
        averageConfidence = averageConfidence,
        verificationThreshold = verificationThreshold,
        isVerified = isVerified,
        verificationCount = verificationCount,
        speakerCharacteristics = speakerCharacteristics,
        metadata = metadata
    )

    companion object {
        fun fromDomain(domain: SpeakerProfile): SpeakerProfileEntity = SpeakerProfileEntity(
            id = domain.id,
            name = domain.name,
            createdAt = domain.createdAt,
            lastUpdated = domain.lastUpdated,
            totalDuration = domain.totalDuration,
            averageConfidence = domain.averageConfidence,
            verificationThreshold = domain.verificationThreshold,
            isVerified = domain.isVerified,
            verificationCount = domain.verificationCount,
            speakerCharacteristics = domain.speakerCharacteristics,
            metadata = domain.metadata
        )
    }
} 