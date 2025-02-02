package com.example.clicknote.domain.model

enum class CloudProvider {
    NONE,
    GOOGLE_CLOUD,
    LOCAL_CLOUD
}

data class BackupSettings(
    val isAutoBackupEnabled: Boolean = false,
    val backupFrequency: BackupFrequency = BackupFrequency.DAILY,
    val cloudProvider: CloudProvider = CloudProvider.NONE,
    val lastBackupTimestamp: Long = 0L,
    val backupOnWifiOnly: Boolean = true,
    val backupAudioFiles: Boolean = true,
    val backupTranscriptions: Boolean = true,
    val backupSettings: Boolean = true,
    val maxBackupSize: Long = 1024 * 1024 * 1024 // 1GB default
)

enum class BackupFrequency {
    DAILY,
    WEEKLY,
    MONTHLY
} 