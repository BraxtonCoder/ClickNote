package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.domain.model.BackupType
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
    private val _searchFilters = MutableStateFlow(BackupSearchFilters())
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

        val typeStats = backups.groupBy { it.backupType }.map { (type, typeBackups) ->
            BackupTypeStats(
                type = type,
                count = typeBackups.size,
                totalSize = typeBackups.sumOf { it.size },
                averageSize = typeBackups.sumOf { it.size } / typeBackups.size
            )
        }

        return BackupStats(
            totalBackups = totalBackups,
            totalSize = totalSize,
            averageSize = averageSize,
            lastBackupTime = lastBackupTime,
            monthlyStats = monthlyStats,
            typeStats = typeStats,
            successRate = calculateSuccessRate(backups)
        )
    }

    private fun calculateSuccessRate(backups: List<BackupInfo>): Float {
        return if (backups.isEmpty()) 0f else 1f // Placeholder
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

    fun searchBackups(query: String? = null, filters: BackupSearchFilters? = null): Flow<List<BackupInfo>> = flow {
        val backups = backupService.listBackups()
        emit(backups.filter { backup ->
            matchesFilters(backup, filters ?: _searchFilters.value) &&
            (query.isNullOrBlank() || matchesQuery(backup, query))
        })
    }

    private fun matchesFilters(backup: BackupInfo, filters: BackupSearchFilters): Boolean {
        return filters.run {
            val matchesDateRange = when (dateRange) {
                DateRange.ALL -> true
                DateRange.TODAY -> {
                    val today = LocalDate.now()
                    backup.toLocalDate() == today
                }
                DateRange.LAST_7_DAYS -> {
                    val weekAgo = LocalDateTime.now().minusDays(7)
                    backup.isAfter(weekAgo)
                }
                DateRange.LAST_30_DAYS -> {
                    val monthAgo = LocalDateTime.now().minusDays(30)
                    backup.isAfter(monthAgo)
                }
                DateRange.CUSTOM -> {
                    customStartDate?.let { start ->
                        customEndDate?.let { end ->
                            !backup.createdAt.isBefore(start) && !backup.createdAt.isAfter(end)
                        }
                    } ?: true
                }
            }

            val matchesType = backupType == null || backup.backupType == backupType
            val matchesSize = when (sizeFilter) {
                SizeFilter.ALL -> true
                SizeFilter.LESS_THAN_10MB -> backup.size < 10 * 1024 * 1024
                SizeFilter.LESS_THAN_100MB -> backup.size < 100 * 1024 * 1024
                SizeFilter.MORE_THAN_100MB -> backup.size >= 100 * 1024 * 1024
            }

            matchesDateRange && matchesType && matchesSize
        }
    }

    private fun matchesQuery(backup: BackupInfo, query: String): Boolean {
        return backup.id.contains(query, ignoreCase = true) ||
               backup.metadata.entries.any { (_, value) -> value.contains(query, ignoreCase = true) }
    }

    fun trackBackupStarted(backupInfo: BackupInfo) {
        analyticsService.track(
            "backup_started",
            mapOf(
                "backup_id" to backupInfo.id,
                "backup_type" to backupInfo.backupType.name,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    fun trackBackupCompleted(
        backupInfo: BackupInfo,
        duration: Long,
        fileCount: Int,
        totalSize: Long
    ) {
        analyticsService.track(
            "backup_completed",
            mapOf(
                "backup_id" to backupInfo.id,
                "backup_type" to backupInfo.backupType.name,
                "duration_ms" to duration,
                "file_count" to fileCount,
                "total_size_bytes" to totalSize,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    fun trackBackupError(
        backupInfo: BackupInfo,
        error: String,
        phase: String
    ) {
        analyticsService.track(
            "backup_error",
            mapOf(
                "backup_id" to backupInfo.id,
                "backup_type" to backupInfo.backupType.name,
                "error" to error,
                "phase" to phase,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    fun trackBackupRestoreStarted(backupInfo: BackupInfo) {
        analyticsService.track(
            "backup_restore_started",
            mapOf(
                "backup_id" to backupInfo.id,
                "backup_type" to backupInfo.backupType.name,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    fun trackBackupRestoreCompleted(
        backupInfo: BackupInfo,
        duration: Long,
        fileCount: Int,
        totalSize: Long
    ) {
        analyticsService.track(
            "backup_restore_completed",
            mapOf(
                "backup_id" to backupInfo.id,
                "backup_type" to backupInfo.backupType.name,
                "duration_ms" to duration,
                "file_count" to fileCount,
                "total_size_bytes" to totalSize,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    fun trackBackupRestoreError(
        backupInfo: BackupInfo,
        error: String,
        phase: String
    ) {
        analyticsService.track(
            "backup_restore_error",
            mapOf(
                "backup_id" to backupInfo.id,
                "backup_type" to backupInfo.backupType.name,
                "error" to error,
                "phase" to phase,
                "timestamp" to System.currentTimeMillis()
            )
        )
    }

    fun trackBackupDeleted(info: BackupInfo) {
        val daysBetween = ChronoUnit.DAYS.between(
            info.createdAt.toLocalDate(),
            LocalDate.now()
        )
        analyticsService.track(
            "backup_deleted",
            mapOf(
                "backup_id" to info.id,
                "backup_age_days" to daysBetween,
                "backup_type" to info.backupType.name,
                "backup_size_bytes" to info.size,
                "timestamp" to System.currentTimeMillis()
            )
        )
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
        val typeStats: List<BackupTypeStats>,
        val successRate: Float
    )

    data class MonthlyBackupStats(
        val month: String,
        val count: Int,
        val totalSize: Long,
        val averageSize: Long
    )

    data class BackupTypeStats(
        val type: BackupType,
        val count: Int,
        val totalSize: Long,
        val averageSize: Long
    )

    data class StorageUsagePoint(
        val timestamp: LocalDateTime,
        val usageBytes: Long
    )

    data class BackupSearchFilters(
        val dateRange: DateRange = DateRange.ALL,
        val customStartDate: LocalDateTime? = null,
        val customEndDate: LocalDateTime? = null,
        val backupType: BackupType? = null,
        val sizeFilter: SizeFilter = SizeFilter.ALL
    )

    enum class DateRange {
        ALL,
        TODAY,
        LAST_7_DAYS,
        LAST_30_DAYS,
        CUSTOM
    }

    enum class SizeFilter {
        ALL,
        LESS_THAN_10MB,
        LESS_THAN_100MB,
        MORE_THAN_100MB
    }
}