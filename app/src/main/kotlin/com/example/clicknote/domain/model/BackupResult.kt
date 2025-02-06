package com.example.clicknote.domain.model

sealed class BackupResult {
    data class Success(
        val backup: BackupInfo,
        val changedFiles: Int,
        val totalSize: Long,
        val compressionRatio: Float
    ) : BackupResult()

    data class Error(
        val message: String,
        val cause: Throwable? = null
    ) : BackupResult()

    data class Progress(
        val percentage: Float,
        val currentFile: String,
        val processedFiles: Int,
        val totalFiles: Int
    ) : BackupResult()
} 