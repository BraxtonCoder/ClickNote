package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.domain.model.BackupSettings
import com.example.clicknote.domain.model.CompressionLevel
import com.example.clicknote.domain.model.CloudStorageProvider
import com.example.clicknote.domain.service.AnalyticsService
import com.example.clicknote.domain.service.BackupService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import org.json.JSONObject
import java.time.LocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupAnalyticsService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupService: BackupService,
    private val analyticsService: AnalyticsService
) {
    private val _backupStats = MutableStateFlow<BackupStats?>(null)
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    suspend fun updateBackupStats() {
        val backups = backupService.listBackups()
        val stats = calculateBackupStats(backups)
        _backupStats.emit(stats)
    }

    private fun calculateBackupStats(backups: List<BackupInfo>): BackupStats {
        val totalBackups = backups.size
        val totalSize = backups.sumOf { it.size }
        val averageSize = if (totalBackups > 0) totalSize / totalBackups else 0
        val lastBackupTime = backups.maxByOrNull { it.createdAt }?.createdAt ?: LocalDateTime.now()
        
        val backupsByMonth = backups.groupBy { backup -> 
            dateFormatter.format(backup.createdAt)
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
        return 0.0f // Placeholder
    }

    private fun calculateSuccessRate(backups: List<BackupInfo>): Float {
        return 0.0f // Placeholder
    }

    fun getBackupStats(): Flow<BackupStats?> = _backupStats

    fun getStorageUsageOverTime(): Flow<List<StorageUsagePoint>> {
        return _backupStats.map { stats ->
            stats?.monthlyStats?.map { monthly ->
                StorageUsagePoint(
                    timestamp = LocalDateTime.now(),
                    usageBytes = monthly.totalSize
                )
            } ?: emptyList()
        }
    }

    fun trackBackupStarted(settings: BackupSettings) {
        val props = JSONObject()
        putSafely(props, "backup_frequency", settings.backupFrequency.toHours())
        putSafely(props, "backup_audio_files", settings.backupAudioFiles)
        putSafely(props, "backup_settings", settings.backupSettings)
        putSafely(props, "compression_level", settings.compressionLevel.toString())
        putSafely(props, "cloud_provider", settings.cloudProvider.toString())
        analyticsService.trackEvent("backup_started", props.toMap())
    }

    fun trackBackupCompleted(info: BackupInfo, duration: Long) {
        val props = JSONObject()
        putSafely(props, "backup_id", info.id)
        putSafely(props, "size_mb", info.size / (1024 * 1024))
        putSafely(props, "note_count", info.noteCount)
        putSafely(props, "audio_count", info.audioCount)
        putSafely(props, "duration_seconds", duration / 1000)
        putSafely(props, "compression_level", info.compressionLevel.toString())
        putSafely(props, "is_encrypted", info.isEncrypted)
        putSafely(props, "cloud_provider", info.cloudStorageProvider.toString())
        analyticsService.trackEvent("backup_completed", props.toMap())
    }

    fun trackBackupError(error: String, info: BackupInfo?) {
        val props = JSONObject()
        putSafely(props, "error_message", error)
        info?.let { backup ->
            putSafely(props, "backup_id", backup.id)
            putSafely(props, "size_mb", backup.size / (1024 * 1024))
            putSafely(props, "note_count", backup.noteCount)
            putSafely(props, "audio_count", backup.audioCount)
            putSafely(props, "compression_level", backup.compressionLevel.toString())
            putSafely(props, "is_encrypted", backup.isEncrypted)
            putSafely(props, "cloud_provider", backup.cloudStorageProvider.toString())
        }
        analyticsService.trackEvent("backup_error", props.toMap())
    }

    fun trackBackupDeleted(info: BackupInfo) {
        val props = JSONObject()
        putSafely(props, "backup_id", info.id)
        val daysBetween = ChronoUnit.DAYS.between(
            info.createdAt.toLocalDate(),
            LocalDate.now()
        )
        putSafely(props, "backup_age_days", daysBetween)
        putSafely(props, "cloud_provider", info.cloudStorageProvider.toString())
        analyticsService.trackEvent("backup_deleted", props.toMap())
    }

    private fun putSafely(json: JSONObject, key: String, value: Any?) {
        val stringValue = when (value) {
            null -> ""
            is Boolean -> value.toString()
            is Number -> value.toString()
            is String -> value
            else -> value.toString()
        }
        json.put(key, stringValue)
    }

    private fun JSONObject.toMap(): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        val keys = this.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            map[key] = this.get(key).toString()
        }
        return map
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