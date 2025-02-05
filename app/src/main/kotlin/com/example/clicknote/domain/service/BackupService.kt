package com.example.clicknote.domain.service

import java.io.File
import kotlinx.coroutines.flow.Flow

interface BackupService {
    suspend fun createBackup(): Result<File>
    suspend fun restoreBackup(backupFile: File): Result<Unit>
    suspend fun getBackupFiles(): Result<List<File>>
    suspend fun deleteBackup(backupFile: File): Result<Unit>
    suspend fun scheduleAutomaticBackup(intervalHours: Int): Result<Unit>
    suspend fun cancelAutomaticBackup(): Result<Unit>
    fun observeBackupProgress(): Flow<Float>
    fun observeRestoreProgress(): Flow<Float>
    suspend fun validateBackup(backupFile: File): Result<Boolean>
    suspend fun getLastBackupTime(): Result<Long>
} 