package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.RoomConverters
import java.util.UUID

@Entity(tableName = "speaker_profiles")
@TypeConverters(RoomConverters::class)
data class SpeakerProfile(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "voice_signature")
    val voiceSignature: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "last_used")
    val lastUsed: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "use_count")
    val useCount: Int = 1,

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true,

    @ColumnInfo(name = "sample_count")
    val sampleCount: Int = 0,

    @ColumnInfo(name = "embeddings")
    val embeddings: String = "", // Store as comma-separated float values

    @ColumnInfo(name = "average_confidence")
    val averageConfidence: Float = 0f
)

class FloatArrayConverter {
    @TypeConverter
    fun fromString(value: String): FloatArray {
        if (value.isEmpty()) return FloatArray(0)
        return value.split(",").map { it.toFloat() }.toFloatArray()
    }

    @TypeConverter
    fun toString(array: FloatArray): String {
        if (array.isEmpty()) return ""
        return array.joinToString(",")
    }
} 