package com.example.clicknote.domain.model

import java.time.LocalDateTime

data class BackupVersion(
    val versionNumber: Int,
    val createdAt: LocalDateTime,
    val changes: List<BackupChange>,
    val noteCount: Int,
    val audioCount: Int,
    val size: Long,
    val checksum: String,
    val metadata: Map<String, String> = emptyMap()
)

data class BackupChange(
    val type: ChangeType,
    val entityType: EntityType,
    val entityId: String,
    val timestamp: LocalDateTime,
    val details: String? = null
)

enum class ChangeType {
    ADDED,
    MODIFIED,
    DELETED,
    MOVED,
    RESTORED
}

enum class EntityType {
    NOTE,
    FOLDER,
    AUDIO,
    PREFERENCE,
    DATABASE
} 