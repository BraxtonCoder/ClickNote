package com.example.clicknote.domain.model

import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Data class representing backup information for the app
 */
data class BackupInfo(
    val id: String,
    val size: Long,
    val createdAt: LocalDateTime,
    val noteCount: Int,
    val audioCount: Int,
    val compressionLevel: CompressionLevel,
    val isEncrypted: Boolean,
    val cloudStorageProvider: CloudStorageProvider,
    val metadata: Map<String, String> = emptyMap()
) {
    fun getCreationDate(): LocalDate = createdAt.toLocalDate()
    
    fun formatCreationDate(formatter: DateTimeFormatter): String = 
        formatter.format(createdAt)

    companion object {
        const val CURRENT_VERSION = 1

        fun createEmpty() = BackupInfo(
            id = "",
            size = 0L,
            createdAt = LocalDateTime.now(),
            noteCount = 0,
            audioCount = 0,
            compressionLevel = CompressionLevel.NONE,
            isEncrypted = false,
            cloudStorageProvider = CloudStorageProvider.NONE,
            metadata = emptyMap()
        )
    }
}