package com.example.clicknote.data.service

import android.content.Context
import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.domain.service.BackupService
import com.example.clicknote.domain.service.BackupVerificationService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupVerificationServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupService: BackupService
) : BackupVerificationService {

    override suspend fun verifyBackup(backupInfo: BackupInfo): Result<Boolean> = runCatching {
        withContext(Dispatchers.IO) {
            // Check if backup exists
            val backupFile = File(context.filesDir, "backups/${backupInfo.id}")
            if (!backupFile.exists()) {
                return@withContext false
            }

            // Verify file size
            if (backupFile.length() != backupInfo.size) {
                return@withContext false
            }

            // Calculate and verify checksum
            val calculatedChecksum = calculateChecksum(backupFile)
            val storedChecksum = backupInfo.metadata["checksum"] ?: return@withContext false
            
            calculatedChecksum == storedChecksum
        }
    }

    override suspend fun verifyAllBackups(): Result<Map<String, Boolean>> = runCatching {
        withContext(Dispatchers.IO) {
            val backups = backupService.listBackups()
            backups.associate { backup ->
                backup.id to (verifyBackup(backup).getOrNull() ?: false)
            }
        }
    }

    override suspend fun verifyBackupMetadata(backupInfo: BackupInfo): Result<Boolean> = runCatching {
        withContext(Dispatchers.IO) {
            val requiredMetadata = listOf("timestamp", "checksum")
            
            // Check required metadata exists
            if (!backupInfo.metadata.keys.containsAll(requiredMetadata)) {
                return@withContext false
            }

            // Verify timestamp is valid
            val timestamp = backupInfo.metadata["timestamp"]?.toLongOrNull()
            if (timestamp == null || timestamp > System.currentTimeMillis()) {
                return@withContext false
            }

            // Verify checksum format
            val checksum = backupInfo.metadata["checksum"]
            if (checksum == null || !isValidChecksum(checksum)) {
                return@withContext false
            }

            true
        }
    }

    /**
     * Validates a checksum string
     */
    private fun isValidChecksum(checksum: String): Boolean {
        // SHA-256 checksums are 64 characters long and contain only hexadecimal digits
        return checksum.length == 64 && checksum.matches(Regex("[a-fA-F0-9]+"))
    }

    /**
     * Calculates checksum for a file
     */
    private fun calculateChecksum(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
} 