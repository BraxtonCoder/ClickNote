package com.example.clicknote.data.service

import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.domain.service.BackupSearchService
import com.example.clicknote.domain.service.BackupService
import kotlinx.coroutines.flow.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupSearchServiceImpl @Inject constructor(
    private val backupService: BackupService
) : BackupSearchService {

    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    override fun searchBackups(query: String): Flow<List<BackupInfo>> = flow {
        val backups = backupService.listBackups()
        emit(backups.filter { backup ->
            backup.id.contains(query, ignoreCase = true) ||
            backup.metadata.any { (key, value) -> 
                key.contains(query, ignoreCase = true) || 
                value.contains(query, ignoreCase = true)
            }
        })
    }

    override fun filterBackupsByDate(
        startDate: String?,
        endDate: String?
    ): Flow<List<BackupInfo>> = flow {
        val backups = backupService.listBackups()
        val parsedStartDate = startDate?.let { LocalDate.parse(it, dateFormatter) }
        val parsedEndDate = endDate?.let { LocalDate.parse(it, dateFormatter) }
        
        emit(backups.filter { backup ->
            val backupDate = backup.createdAt.toLocalDate()
            when {
                parsedStartDate != null && parsedEndDate != null -> 
                    !backupDate.isBefore(parsedStartDate) && !backupDate.isAfter(parsedEndDate)
                parsedStartDate != null -> 
                    !backupDate.isBefore(parsedStartDate)
                parsedEndDate != null -> 
                    !backupDate.isAfter(parsedEndDate)
                else -> true
            }
        })
    }

    override fun filterBackupsBySize(
        minSize: Long?,
        maxSize: Long?
    ): Flow<List<BackupInfo>> = flow {
        val backups = backupService.listBackups()
        emit(backups.filter { backup ->
            when {
                minSize != null && maxSize != null -> 
                    backup.size in minSize..maxSize
                minSize != null -> 
                    backup.size >= minSize
                maxSize != null -> 
                    backup.size <= maxSize
                else -> true
            }
        })
    }

    override fun filterBackupsByMetadata(
        key: String,
        value: String
    ): Flow<List<BackupInfo>> = flow {
        val backups = backupService.listBackups()
        emit(backups.filter { backup ->
            backup.metadata[key]?.contains(value, ignoreCase = true) == true
        })
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 20
    }
} 