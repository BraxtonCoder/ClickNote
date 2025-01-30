package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.DateTimeConverters
import com.example.clicknote.domain.model.CallRecording
import java.util.UUID

@Entity(
    tableName = "call_recordings",
    indices = [
        Index("created_at"),
        Index("phone_number"),
        Index("sync_status")
    ]
)
@TypeConverters(DateTimeConverters::class)
data class CallRecordingEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "phone_number")
    val phoneNumber: String,

    @ColumnInfo(name = "contact_name")
    val contactName: String? = null,

    @ColumnInfo(name = "duration")
    val duration: Long, // Duration in milliseconds

    @ColumnInfo(name = "audio_path")
    val audioPath: String,

    @ColumnInfo(name = "transcription")
    val transcription: String? = null,

    @ColumnInfo(name = "summary")
    val summary: String? = null,

    @ColumnInfo(name = "is_incoming")
    val isIncoming: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "sync_status")
    val syncStatus: Int = 0
) {
    companion object {
        const val SYNC_STATUS_PENDING = 0
        const val SYNC_STATUS_IN_PROGRESS = 1
        const val SYNC_STATUS_COMPLETED = 2
        const val SYNC_STATUS_FAILED = 3

        fun fromDomain(model: CallRecording) = CallRecordingEntity(
            id = model.id,
            phoneNumber = model.phoneNumber,
            contactName = model.contactName,
            duration = model.duration,
            audioPath = model.audioPath,
            transcription = model.transcription,
            summary = model.summary,
            isIncoming = model.isIncoming,
            createdAt = model.timestamp,
            isDeleted = model.isInTrash,
            deletedAt = model.deletedAt
        )
    }
}

fun CallRecording.toCallRecordingEntity(): CallRecordingEntity = CallRecordingEntity.fromDomain(this) 