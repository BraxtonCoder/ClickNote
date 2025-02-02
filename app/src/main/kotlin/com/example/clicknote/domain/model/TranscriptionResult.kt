package com.example.clicknote.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime
import com.example.clicknote.domain.model.TranscriptionLanguage

data class TranscriptionResult(
    val text: String,
    val language: TranscriptionLanguage,
    val speakers: List<String>
)

@Parcelize
data class TranscriptionSegment(
    val text: String,
    val startTime: Long,
    val endTime: Long,
    val speaker: String?,
    val confidence: Float
) : Parcelable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as TranscriptionSegment

        if (text != other.text) return false
        if (startTime != other.startTime) return false
        if (endTime != other.endTime) return false
        if (speaker != other.speaker) return false
        if (confidence != other.confidence) return false

        return true
    }

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + startTime.hashCode()
        result = 31 * result + endTime.hashCode()
        result = 31 * result + (speaker?.hashCode() ?: 0)
        result = 31 * result + confidence.hashCode()
        return result
    }
}

data class TranscriptionOptions(
    val language: TranscriptionLanguage = TranscriptionLanguage.ENGLISH,
    val detectSpeakers: Boolean = false,
    val useTimestamps: Boolean = true,
    val useOffline: Boolean = false
)

data class Speaker(
    val id: String,
    val name: String = "Speaker ${id}"
) 