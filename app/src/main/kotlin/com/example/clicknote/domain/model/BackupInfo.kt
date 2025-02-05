package com.example.clicknote.domain.model

import java.time.LocalDateTime

/**
 * Data class representing backup information for the app
 */
data class BackupInfo(
    val id: String,
    val name: String,
    val path: String,
    val size: Long,
    val createdAt: LocalDateTime,
    val version: Int,
    val noteCount: Int,
    val audioCount: Int,
    val compressionLevel: CompressionLevel = CompressionLevel.BALANCED,
    val isEncrypted: Boolean = false,
    val cloudStorageProvider: CloudStorageProvider = CloudStorageProvider.NONE,
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
} 