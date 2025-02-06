package com.example.clicknote.domain.model

/**
 * Settings for configuring backup behavior
 */
data class BackupSettings(
    val isAutoBackupEnabled: Boolean = false,
    val backupFrequency: BackupFrequency = BackupFrequency.DAILY,
    val cloudProvider: CloudStorageProvider = CloudStorageProvider.NONE,
    val lastBackupTimestamp: Long = 0L,
    val backupOnWifiOnly: Boolean = true,
    val backupAudioFiles: Boolean = true,
    val backupTranscriptions: Boolean = true,
    val backupSettings: Boolean = true,
    val maxBackupSize: Long = 1024 * 1024 * 1024 // 1GB default
) {
    companion object {
        const val DEFAULT_MAX_BACKUP_SIZE = 1024L * 1024L * 1024L // 1GB
    }
}

/**
 * Frequency options for automatic backups
 */
enum class BackupFrequency {
    DAILY,      // Once per day
    WEEKLY,     // Once per week
    MONTHLY,    // Once per month
    ON_CHANGE;  // Backup whenever changes are made

    companion object {
        fun fromString(value: String): BackupFrequency {
            return try {
                valueOf(value.uppercase())
            } catch (e: IllegalArgumentException) {
                DAILY
            }
        }
    }
} 