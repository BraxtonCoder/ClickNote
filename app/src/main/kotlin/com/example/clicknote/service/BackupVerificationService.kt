package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.*
import com.example.clicknote.util.verifyChecksum
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupVerificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupService: BackupService,
    private val notificationService: BackupNotificationService
) {
    private val tempDir = File(context.cacheDir, "backup_verify")

    init {
        tempDir.mkdirs()
    }

    suspend fun verifyBackup(backup: BackupInfo): BackupVerificationResult {
        try {
            // Download backup to temp directory
            val backupFile = File(tempDir, backup.name)
            // TODO: Download backup file using cloudStorageAdapter

            // Verify checksum
            if (!verifyChecksum(backupFile, backup.checksum)) {
                return BackupVerificationResult.Error("Backup file checksum verification failed")
            }

            // Verify zip integrity
            if (!verifyZipIntegrity(backupFile)) {
                return BackupVerificationResult.Error("Backup file is corrupted")
            }

            // Verify backup contents
            val contentVerification = verifyBackupContents(backupFile)
            if (contentVerification != null) {
                return BackupVerificationResult.Error(contentVerification)
            }

            // Verify version info
            if (!verifyVersionInfo(backup)) {
                return BackupVerificationResult.Error("Version information is invalid")
            }

            return BackupVerificationResult.Success(
                BackupVerificationStats(
                    fileCount = countFiles(backupFile),
                    totalSize = backupFile.length(),
                    compressionRatio = calculateCompressionRatio(backupFile),
                    integrityStatus = "Valid"
                )
            )
        } catch (e: Exception) {
            return BackupVerificationResult.Error(e.message ?: "Verification failed")
        }
    }

    private fun verifyZipIntegrity(file: File): Boolean {
        return try {
            ZipFile(file).use { zip ->
                zip.entries().asSequence().all { entry ->
                    !entry.isDirectory && zip.getInputStream(entry).use { it.readBytes().isNotEmpty() }
                }
            }
        } catch (e: Exception) {
            false
        }
    }

    private fun verifyBackupContents(file: File): String? {
        return try {
            ZipFile(file).use { zip ->
                // Check for required files
                val entries = zip.entries().asSequence().map { it.name }.toSet()
                
                if (!entries.any { it.startsWith("database/") }) {
                    return "Missing database files"
                }

                // Verify each file's integrity
                zip.entries().asSequence().forEach { entry ->
                    zip.getInputStream(entry).use { stream ->
                        if (stream.readBytes().isEmpty()) {
                            return "Empty file found: ${entry.name}"
                        }
                    }
                }

                null // No errors found
            }
        } catch (e: Exception) {
            e.message
        }
    }

    private fun verifyVersionInfo(backup: BackupInfo): Boolean {
        return backup.version >= 1 && backup.createdAt != null
    }

    private fun countFiles(file: File): Int {
        return ZipFile(file).use { zip ->
            zip.entries().asSequence().count { !it.isDirectory }
        }
    }

    private fun calculateCompressionRatio(file: File): Float {
        var uncompressedSize = 0L
        var compressedSize = 0L

        ZipFile(file).use { zip ->
            zip.entries().asSequence().forEach { entry ->
                uncompressedSize += entry.size
                compressedSize += entry.compressedSize
            }
        }

        return if (uncompressedSize > 0) {
            1 - (compressedSize.toFloat() / uncompressedSize.toFloat())
        } else {
            0f
        }
    }

    sealed class BackupVerificationResult {
        data class Success(val stats: BackupVerificationStats) : BackupVerificationResult()
        data class Error(val message: String) : BackupVerificationResult()
    }

    data class BackupVerificationStats(
        val fileCount: Int,
        val totalSize: Long,
        val compressionRatio: Float,
        val integrityStatus: String
    )
} 