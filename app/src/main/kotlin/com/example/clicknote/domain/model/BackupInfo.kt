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
    val backupType: BackupType,
    val metadata: Map<String, String> = emptyMap()
) {
    fun toLocalDate(): LocalDate = createdAt.toLocalDate()

    fun isAfter(other: LocalDateTime): Boolean = createdAt.isAfter(other)

    fun getCreationDate(): LocalDate = createdAt.toLocalDate()
    
    fun formatCreationDate(formatter: DateTimeFormatter): String = 
        formatter.format(createdAt)

    companion object {
        const val CURRENT_VERSION = 1

        fun createEmpty() = BackupInfo(
            id = "",
            size = 0L,
            createdAt = LocalDateTime.now(),
            backupType = BackupType.FULL,
            metadata = emptyMap()
        )
    }
}

enum class BackupType {
    FULL,
    DIFFERENTIAL,
    INCREMENTAL
}