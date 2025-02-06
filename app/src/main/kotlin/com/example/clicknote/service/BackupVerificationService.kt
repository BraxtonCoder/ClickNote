package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.domain.service.BackupService
import com.example.clicknote.data.storage.CloudStorageAdapter
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.util.zip.ZipFile
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Singleton
class BackupVerificationService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupService: com.example.clicknote.domain.service.BackupService,
    private val notificationService: BackupNotificationService,
    private val cloudStorageAdapter: CloudStorageAdapter
) {
    private val tempDir = File(context.cacheDir, "backup_verify")

    init {
        tempDir.mkdirs()
    }

    suspend fun verifyBackup(backup: BackupInfo): BackupVerificationResult = withContext(Dispatchers.IO) {
        try {
            // Download backup to temp directory
            val backupFile = File(tempDir, backup.id)
            val remotePath = "backups/${backup.metadata["userId"]}/${backup.id}"
            cloudStorageAdapter.downloadFile(remotePath, backupFile)

            // Verify file size
            if (backupFile.length() != backup.size) {
                return@withContext BackupVerificationResult.Error("File size mismatch")
            }

            // Verify checksum if available
            backup.metadata["checksum"]?.let { storedChecksum ->
                if (!verifyChecksum(backupFile, storedChecksum)) {
                    return@withContext BackupVerificationResult.Error("Checksum verification failed")
                }
            }

            // Verify zip integrity
            if (!verifyZipIntegrity(backupFile)) {
                return@withContext BackupVerificationResult.Error("Backup file is corrupted")
            }

            // Verify backup contents
            val contentVerification = verifyBackupContents(backupFile)
            if (contentVerification != null) {
                return@withContext BackupVerificationResult.Error(contentVerification)
            }

            // Verify version info
            val version = backup.metadata["version"]?.toIntOrNull() ?: 1
            if (version < 1) {
                return@withContext BackupVerificationResult.Error("Invalid backup version")
            }

            BackupVerificationResult.Success(
                BackupVerificationStats(
                    fileCount = countFiles(backupFile),
                    totalSize = backupFile.length(),
                    compressionRatio = calculateCompressionRatio(backupFile),
                    integrityStatus = "Valid"
                )
            )
        } catch (e: Exception) {
            BackupVerificationResult.Error(e.message ?: "Verification failed")
        } finally {
            // Cleanup temp files
            tempDir.listFiles()?.forEach { it.delete() }
        }
    }

    private fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        return try {
            val calculatedChecksum = calculateChecksum(file)
            calculatedChecksum == expectedChecksum
        } catch (e: Exception) {
            false
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

    private fun calculateChecksum(file: File): String {
        val digest = java.security.MessageDigest.getInstance("SHA-256")
        file.inputStream().use { fis ->
            val buffer = ByteArray(8192)
            var bytesRead: Int
            while (fis.read(buffer).also { bytesRead = it } != -1) {
                digest.update(buffer, 0, bytesRead)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
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