package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.BackupInfo
import kotlinx.coroutines.flow.Flow

interface BackupService {
    suspend fun createBackup(): Result<BackupInfo>
    suspend fun restoreBackup(backupId: String): Result<Unit>
    suspend fun listBackups(): List<BackupInfo>
    suspend fun deleteBackup(backupId: String): Result<Unit>
    suspend fun scheduleAutomaticBackup(intervalHours: Int): Result<Unit>
    suspend fun cancelAutomaticBackup(): Result<Unit>
    fun observeBackupProgress(): Flow<Float>
    fun observeRestoreProgress(): Flow<Float>
    suspend fun validateBackup(backupId: String): Result<Boolean>
    suspend fun getLastBackupTime(): Result<Long>
} 