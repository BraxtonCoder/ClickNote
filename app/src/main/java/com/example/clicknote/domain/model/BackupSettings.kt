package com.example.clicknote.domain.model

data class BackupSettings(
    val isAutoBackupEnabled: Boolean = false,
    val backupIntervalHours: Int = 24,
    val maxBackupCount: Int = 10,
    val retentionDays: Int = 30,
    val encryptBackups: Boolean = true,
    val includeAudioFiles: Boolean = true,
    val cloudStorageProvider: CloudStorageProvider = CloudStorageProvider.FIREBASE,
    val compressionLevel: CompressionLevel = CompressionLevel.NORMAL
)

enum class CloudStorageProvider {
    FIREBASE,
    AWS,
    AZURE,
    GOOGLE_CLOUD
}

enum class CompressionLevel(val ratio: Float) {
    NONE(1.0f),
    FAST(0.8f),
    NORMAL(0.6f),
    HIGH(0.4f),
    MAXIMUM(0.2f)
} 