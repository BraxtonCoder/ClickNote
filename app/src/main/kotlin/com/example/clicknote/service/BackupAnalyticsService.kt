package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONObject

@Singleton
class BackupAnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupService: BackupService,
    private val analyticsService: AnalyticsService
) {
    private val _backupStats = MutableStateFlow<BackupStats?>(null)

    suspend fun updateBackupStats() {
        val backups = backupService.listBackups()
        val stats = calculateBackupStats(backups)
        _backupStats.emit(stats)
    }

    private fun calculateBackupStats(backups: List<BackupInfo>): BackupStats {
        val totalBackups = backups.size
        val totalSize = backups.sumOf { it.size }
        val averageSize = if (totalBackups > 0) totalSize / totalBackups else 0
        val lastBackupTime = backups.maxOfOrNull { it.createdAt } ?: LocalDateTime.now()
        
        val backupsByMonth = backups.groupBy { 
            "${it.createdAt.year}-${it.createdAt.monthValue}" 
        }
        
        val monthlyStats = backupsByMonth.map { (month, monthBackups) ->
            MonthlyBackupStats(
                month = month,
                count = monthBackups.size,
                totalSize = monthBackups.sumOf { it.size },
                averageSize = monthBackups.sumOf { it.size } / monthBackups.size
            )
        }

        val compressionStats = backups.groupBy { it.compressionLevel }.map { (level, levelBackups) ->
            CompressionStats(
                level = level,
                count = levelBackups.size,
                totalSize = levelBackups.sumOf { it.size },
                averageSize = levelBackups.sumOf { it.size } / levelBackups.size,
                averageCompressionRatio = calculateCompressionRatio(levelBackups)
            )
        }

        return BackupStats(
            totalBackups = totalBackups,
            totalSize = totalSize,
            averageSize = averageSize,
            lastBackupTime = lastBackupTime,
            monthlyStats = monthlyStats,
            compressionStats = compressionStats,
            successRate = calculateSuccessRate(backups)
        )
    }

    private fun calculateCompressionRatio(backups: List<BackupInfo>): Float {
        // Implementation to calculate average compression ratio
        return 0.0f // Placeholder
    }

    private fun calculateSuccessRate(backups: List<BackupInfo>): Float {
        // Implementation to calculate backup success rate
        return 0.0f // Placeholder
    }

    fun getBackupStats(): Flow<BackupStats?> = _backupStats

    fun getStorageUsageOverTime(): Flow<List<StorageUsagePoint>> {
        return _backupStats.map { stats ->
            stats?.monthlyStats?.map { monthly ->
                StorageUsagePoint(
                    timestamp = LocalDateTime.now(), // Parse from month string
                    usageBytes = monthly.totalSize
                )
            } ?: emptyList()
        }
    }

    fun trackBackupStarted(settings: BackupSettings) {
        val properties = JSONObject().apply {
            put("auto_backup_enabled", settings.isAutoBackupEnabled)
            put("backup_interval_hours", settings.backupIntervalHours)
            put("include_audio", settings.includeAudioFiles)
            put("encryption_enabled", settings.encryptBackups)
            put("compression_level", settings.compressionLevel.name)
            put("cloud_provider", settings.cloudStorageProvider.name)
        }
        analyticsService.track("Backup Started", properties)
    }

    fun trackBackupCompleted(backupInfo: BackupInfo, duration: Long) {
        val properties = JSONObject().apply {
            put("backup_id", backupInfo.id)
            put("size_mb", backupInfo.size / (1024 * 1024))
            put("note_count", backupInfo.noteCount)
            put("audio_count", backupInfo.audioCount)
            put("duration_seconds", duration / 1000)
            put("compression_level", backupInfo.compressionLevel.name)
            put("is_encrypted", backupInfo.isEncrypted)
            put("cloud_provider", backupInfo.cloudStorageProvider.name)
        }
        analyticsService.track("Backup Completed", properties)
    }

    fun trackBackupFailed(error: String, phase: String) {
        val properties = JSONObject().apply {
            put("error_message", error)
            put("phase", phase)
        }
        analyticsService.track("Backup Failed", properties)
    }

    fun trackBackupRestoreStarted(backupInfo: BackupInfo) {
        val properties = JSONObject().apply {
            put("backup_id", backupInfo.id)
            put("size_mb", backupInfo.size / (1024 * 1024))
            put("note_count", backupInfo.noteCount)
            put("audio_count", backupInfo.audioCount)
            put("is_encrypted", backupInfo.isEncrypted)
            put("cloud_provider", backupInfo.cloudStorageProvider.name)
        }
        analyticsService.track("Backup Restore Started", properties)
    }

    fun trackBackupRestoreCompleted(backupInfo: BackupInfo, duration: Long) {
        val properties = JSONObject().apply {
            put("backup_id", backupInfo.id)
            put("size_mb", backupInfo.size / (1024 * 1024))
            put("note_count", backupInfo.noteCount)
            put("audio_count", backupInfo.audioCount)
            put("duration_seconds", duration / 1000)
            put("is_encrypted", backupInfo.isEncrypted)
            put("cloud_provider", backupInfo.cloudStorageProvider.name)
        }
        analyticsService.track("Backup Restore Completed", properties)
    }

    fun trackBackupRestoreFailed(error: String, phase: String) {
        val properties = JSONObject().apply {
            put("error_message", error)
            put("phase", phase)
        }
        analyticsService.track("Backup Restore Failed", properties)
    }

    fun trackBackupSettingsChanged(settings: BackupSettings) {
        val properties = JSONObject().apply {
            put("auto_backup_enabled", settings.isAutoBackupEnabled)
            put("backup_interval_hours", settings.backupIntervalHours)
            put("include_audio", settings.includeAudioFiles)
            put("encryption_enabled", settings.encryptBackups)
            put("compression_level", settings.compressionLevel.name)
            put("cloud_provider", settings.cloudStorageProvider.name)
        }
        analyticsService.track("Backup Settings Changed", properties)
    }

    fun trackBackupDeleted(backupInfo: BackupInfo) {
        val properties = JSONObject().apply {
            put("backup_id", backupInfo.id)
            put("size_mb", backupInfo.size / (1024 * 1024))
            put("age_days", backupInfo.createdAt.until(LocalDateTime.now()).toDays())
            put("cloud_provider", backupInfo.cloudStorageProvider.name)
        }
        analyticsService.track("Backup Deleted", properties)
    }

    data class BackupStats(
        val totalBackups: Int,
        val totalSize: Long,
        val averageSize: Long,
        val lastBackupTime: LocalDateTime,
        val monthlyStats: List<MonthlyBackupStats>,
        val compressionStats: List<CompressionStats>,
        val successRate: Float
    )

    data class MonthlyBackupStats(
        val month: String,
        val count: Int,
        val totalSize: Long,
        val averageSize: Long
    )

    data class CompressionStats(
        val level: CompressionLevel,
        val count: Int,
        val totalSize: Long,
        val averageSize: Long,
        val averageCompressionRatio: Float
    )

    data class StorageUsagePoint(
        val timestamp: LocalDateTime,
        val usageBytes: Long
    )
} 