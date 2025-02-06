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

    fun trackBackupStarted(settings: BackupSearchFilters) {
        val props = JSONObject()
        putSafely(props, "backup_type", settings.backupType?.name ?: "FULL")
        putSafely(props, "size_filter", settings.sizeFilter.name)
        putSafely(props, "date_range", settings.dateRange.name)
        analyticsService.trackEvent("backup_started", props.toMap())
    }

    fun trackBackupCompleted(info: BackupInfo, duration: Long) {
        val props = JSONObject()
        putSafely(props, "backup_id", info.id)
        putSafely(props, "size_mb", info.size / (1024 * 1024))
        putSafely(props, "backup_type", info.backupType.name)
        putSafely(props, "duration_seconds", duration / 1000)
        putSafely(props, "metadata_count", info.metadata.size)
        analyticsService.trackEvent("backup_completed", props.toMap())
    }

    fun trackBackupError(error: String, info: BackupInfo?) {
        val props = JSONObject()
        putSafely(props, "error_message", error)
        info?.let { backup ->
            putSafely(props, "backup_id", backup.id)
            putSafely(props, "size_mb", backup.size / (1024 * 1024))
            putSafely(props, "backup_type", backup.backupType.name)
            putSafely(props, "metadata_count", backup.metadata.size)
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
        putSafely(props, "backup_type", info.backupType.name)
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