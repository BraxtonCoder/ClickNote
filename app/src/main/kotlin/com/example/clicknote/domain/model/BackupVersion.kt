package com.example.clicknote.domain.model

import java.time.LocalDateTime

/**
 * Represents a version of a backup with its changes and metadata
 */
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

/**
 * Represents a change made in a backup version
 */
data class BackupChange(
    val type: ChangeType,
    val entityType: BackupEntityType,
    val entityId: String,
    val timestamp: LocalDateTime,
    val details: String? = null
)

/**
 * Types of changes that can occur in a backup
 */
enum class ChangeType {
    ADDED,      // New entity added
    MODIFIED,   // Existing entity modified
    DELETED,    // Entity deleted
    MOVED,      // Entity moved (e.g., note moved to different folder)
    RESTORED    // Entity restored from recycle bin
}

/**
 * Types of entities that can be backed up
 */
enum class BackupEntityType {
    NOTE,       // User notes
    FOLDER,     // Note folders/categories
    AUDIO,      // Audio recordings
    PREFERENCE, // User preferences
    DATABASE    // Database schema/data
} 