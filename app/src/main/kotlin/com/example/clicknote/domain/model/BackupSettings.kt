package com.example.clicknote.domain.model

/**
 * Settings for configuring backup behavior
 */
data class BackupSettings(
    val backupFrequency: BackupFrequency = BackupFrequency.DAILY,
    val backupAudioFiles: Boolean = true,
    val backupSettings: Boolean = true,
    val compressionLevel: CompressionLevel = CompressionLevel.MEDIUM,
    val cloudProvider: CloudStorageProvider = CloudStorageProvider.NONE,
    val maxBackupSize: Long = DEFAULT_MAX_BACKUP_SIZE
) {
    companion object {
        const val DEFAULT_MAX_BACKUP_SIZE = 1024L * 1024L * 1024L // 1GB
    }
}

/**
 * Frequency options for automatic backups
 */
enum class BackupFrequency(private val hours: Int) {
    HOURLY(1),
    DAILY(24),
    WEEKLY(24 * 7),
    MONTHLY(24 * 30);

    fun toHours(): Int = hours
}