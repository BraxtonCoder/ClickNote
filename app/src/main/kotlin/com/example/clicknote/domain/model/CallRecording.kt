package com.example.clicknote.domain.model

import java.time.LocalDateTime

data class CallRecording(
    val id: String,
    val phoneNumber: String,
    val contactName: String?,
    val duration: Long,
    val audioPath: String,
    val transcription: String?,
    val summary: String?,
    val isIncoming: Boolean,
    val timestamp: Long,
    val isInTrash: Boolean,
    val deletedAt: Long?
) {
    companion object {
        fun create(
            phoneNumber: String,
            contactName: String?,
            timestamp: Long,
            duration: Long,
            audioFilePath: String,
            transcription: String,
            summary: String?,
            isIncoming: Boolean
        ): CallRecording {
            return CallRecording(
                id = generateId(phoneNumber, timestamp),
                phoneNumber = phoneNumber,
                contactName = contactName,
                duration = duration,
                audioPath = audioFilePath,
                transcription = transcription,
                summary = summary,
                isIncoming = isIncoming,
                timestamp = timestamp,
                isInTrash = false,
                deletedAt = null
            )
        }

        private fun generateId(phoneNumber: String, timestamp: Long): String {
            return "${phoneNumber}_$timestamp"
        }

        fun fromEntity(entity: com.example.clicknote.data.entity.CallRecordingEntity) = CallRecording(
            id = entity.id,
            phoneNumber = entity.phoneNumber,
            contactName = entity.contactName,
            duration = entity.duration,
            audioPath = entity.audioPath,
            transcription = entity.transcription,
            summary = entity.summary,
            isIncoming = entity.isIncoming,
            timestamp = entity.createdAt,
            isInTrash = entity.isDeleted,
            deletedAt = entity.deletedAt
        )
    }
} 