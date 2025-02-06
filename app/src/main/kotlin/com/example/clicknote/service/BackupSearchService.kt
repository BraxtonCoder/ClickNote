package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.domain.model.BackupType
import com.example.clicknote.domain.service.BackupService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupSearchService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupService: BackupService,
    private val analyticsService: BackupAnalyticsService
) {
    private val _searchResults = MutableStateFlow<List<BackupInfo>>(emptyList())
    private val _searchFilters = MutableStateFlow(BackupSearchFilters())

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

    fun updateFilters(filters: BackupSearchFilters) {
        _searchFilters.value = filters
    }

    fun getSearchFilters(): Flow<BackupSearchFilters> = _searchFilters.asStateFlow()

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