package com.example.clicknote.service.backup

import kotlinx.coroutines.flow.Flow
import java.io.File

interface BackupService {
    suspend fun createBackup(): BackupResult
    suspend fun restoreBackup(backupFile: File): RestoreResult
    suspend fun listBackups(): List<BackupInfo>
    suspend fun deleteBackup(backupId: String)
    suspend fun scheduleBackup(frequency: BackupFrequency)
    suspend fun cancelScheduledBackup()
    fun observeBackupStatus(): Flow<BackupStatus>
}

data class BackupInfo(
    val id: String,
    val timestamp: Long,
    val size: Long,
    val itemCount: Int,
    val status: BackupStatus
)

sealed class BackupStatus {
    object Idle : BackupStatus()
    data class InProgress(val progress: Int, val total: Int) : BackupStatus()
    data class Completed(val timestamp: Long) : BackupStatus()
    data class Failed(val error: String) : BackupStatus()
}

sealed class BackupResult {
    data class Success(val backupInfo: BackupInfo) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(val itemCount: Int) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

enum class BackupFrequency {
    DAILY,
    WEEKLY,
    MONTHLY,
    NEVER
} 