package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.BackupInfo

interface BackupVerificationService {
    /**
     * Verifies a backup file's integrity
     */
    suspend fun verifyBackup(backupInfo: BackupInfo): Result<Boolean>

    /**
     * Verifies all backups in a directory
     */
    suspend fun verifyAllBackups(): Result<Map<String, Boolean>>

    /**
     * Verifies backup metadata
     */
    suspend fun verifyBackupMetadata(backupInfo: BackupInfo): Result<Boolean>
} 