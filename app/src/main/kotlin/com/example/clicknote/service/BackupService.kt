package com.example.clicknote.service

import android.content.Context
import android.net.Uri
import com.example.clicknote.data.storage.CloudStorageAdapter
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.repository.UserRepository
import com.example.clicknote.util.BackupEncryption
import com.example.clicknote.util.calculateChecksum
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

@Singleton
class BackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cloudStorageAdapter: CloudStorageAdapter,
    private val userRepository: UserRepository,
    private val backupEncryption: BackupEncryption
) {
    private val backupDir = File(context.filesDir, "backups")
    private val tempDir = File(context.cacheDir, "backup_temp")
    private val versionFile = File(backupDir, "versions.json")
    private val changeLogFile = File(backupDir, "changelog.json")

    init {
        backupDir.mkdirs()
        tempDir.mkdirs()
    }

    suspend fun createBackup(
        name: String? = null,
        changes: List<BackupChange> = emptyList(),
        onProgress: (Int) -> Unit
    ): Flow<BackupResult> = flow {
        try {
            // Create backup directory structure
            val timestamp = LocalDateTime.now()
            val backupName = name ?: "backup_${timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}"
            val backupFile = File(tempDir, "$backupName.zip")
            val encryptedFile = File(tempDir, "$backupName.encrypted")

            // Track files to backup
            val filesToBackup = mutableListOf<Pair<File, String>>()
            var totalSize = 0L
            var noteCount = 0
            var audioCount = 0

            // Add database
            val dbFile = context.getDatabasePath("clicknote.db")
            filesToBackup.add(dbFile to "database/")
            totalSize += dbFile.length()

            // Add audio files
            val audioDir = File(context.filesDir, "audio")
            if (audioDir.exists()) {
                audioDir.walk().forEach { file ->
                    if (file.isFile) {
                        filesToBackup.add(file to "audio/")
                        totalSize += file.length()
                        audioCount++
                    }
                }
            }

            // Add preferences
            val prefsDir = File(context.dataDir, "shared_prefs")
            if (prefsDir.exists()) {
                prefsDir.walk().forEach { file ->
                    if (file.isFile && file.extension == "xml") {
                        filesToBackup.add(file to "preferences/")
                        totalSize += file.length()
                    }
                }
            }

            // Create ZIP file
            var processedSize = 0L
            ZipOutputStream(backupFile.outputStream()).use { zip ->
                filesToBackup.forEach { (file, pathPrefix) ->
                    addFileToZip(zip, file, pathPrefix) { progress ->
                        processedSize += progress
                        onProgress((processedSize * 100 / totalSize).toInt())
                    }
                }
            }

            // Calculate checksum
            val checksum = calculateChecksum(backupFile)

            // Create version info
            val version = BackupVersion(
                versionNumber = getNextVersionNumber(),
                createdAt = timestamp,
                changes = changes,
                noteCount = noteCount,
                audioCount = audioCount,
                size = backupFile.length(),
                checksum = checksum
            )

            // Save version info
            saveVersionInfo(version)

            // Encrypt the backup
            backupEncryption.encryptFile(backupFile, encryptedFile)
            backupFile.delete()

            // Upload to cloud storage
            val remotePath = "backups/${userRepository.getCurrentUser()?.id}/$backupName.encrypted"
            val url = cloudStorageAdapter.uploadFile(encryptedFile, remotePath)
            encryptedFile.delete()

            emit(BackupResult.Success(url))
        } catch (e: Exception) {
            emit(BackupResult.Error(e.message ?: "Backup failed"))
        }
    }

    private suspend fun getNextVersionNumber(): Int {
        return withContext(Dispatchers.IO) {
            if (!versionFile.exists()) return@withContext 1
            // Read and parse version file to get the latest version number
            val versions = parseVersionFile()
            (versions.maxOfOrNull { it.versionNumber } ?: 0) + 1
        }
    }

    private suspend fun saveVersionInfo(version: BackupVersion) {
        withContext(Dispatchers.IO) {
            val versions = if (versionFile.exists()) {
                parseVersionFile()
            } else {
                emptyList()
            }
            
            // Add new version and save
            val updatedVersions = versions + version
            // Save to version file (implementation depends on your JSON serialization library)
        }
    }

    private suspend fun parseVersionFile(): List<BackupVersion> {
        return withContext(Dispatchers.IO) {
            if (!versionFile.exists()) return@withContext emptyList()
            // Parse version file and return list of versions
            // Implementation depends on your JSON serialization library
            emptyList() // Placeholder
        }
    }

    suspend fun restoreBackup(uri: Uri, onProgress: (Int) -> Unit): Flow<BackupResult> = flow {
        try {
            val tempFile = File(tempDir, "temp_backup.encrypted")
            val decryptedFile = File(tempDir, "temp_backup.zip")

            // Copy backup file to temp directory
            context.contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Decrypt the backup
            backupEncryption.decryptFile(tempFile, decryptedFile)
            tempFile.delete()

            // Extract and restore files
            ZipInputStream(decryptedFile.inputStream()).use { zip ->
                var entry: ZipEntry?
                while (zip.nextEntry.also { entry = it } != null) {
                    entry?.let { zipEntry ->
                        val targetFile = when {
                            zipEntry.name.startsWith("database/") -> {
                                context.getDatabasePath("clicknote.db")
                            }
                            zipEntry.name.startsWith("audio/") -> {
                                File(context.filesDir, "audio/${zipEntry.name.substringAfter("audio/")}")
                            }
                            zipEntry.name.startsWith("preferences/") -> {
                                File(context.dataDir, "shared_prefs/${zipEntry.name.substringAfter("preferences/")}")
                            }
                            else -> null
                        }

                        targetFile?.let {
                            it.parentFile?.mkdirs()
                            it.outputStream().use { output ->
                                zip.copyTo(output)
                            }
                            onProgress((zip.bytesRead * 100 / decryptedFile.length()).toInt())
                        }
                    }
                }
            }

            decryptedFile.delete()
            emit(BackupResult.Success("Backup restored successfully"))
        } catch (e: Exception) {
            emit(BackupResult.Error(e.message ?: "Restore failed"))
        }
    }

    suspend fun listBackups(): List<BackupInfo> = withContext(Dispatchers.IO) {
        val userId = userRepository.getCurrentUser()?.id ?: return@withContext emptyList()
        val backups = cloudStorageAdapter.listFiles("backups/$userId")
        
        backups.map { file ->
            BackupInfo(
                name = file.name.removeSuffix(".encrypted"),
                size = file.size,
                createdAt = file.metadata.createdAt,
                path = file.path
            )
        }.sortedByDescending { it.createdAt }
    }

    suspend fun deleteBackup(path: String) {
        cloudStorageAdapter.deleteFile(path)
    }

    private fun addFileToZip(zip: ZipOutputStream, file: File, pathPrefix: String, onProgress: (Int) -> Unit) {
        val entry = ZipEntry(pathPrefix + file.name)
        zip.putNextEntry(entry)
        file.inputStream().use { input ->
            input.copyTo(zip)
        }
        zip.closeEntry()
        onProgress((entry.size * 100 / file.length()).toInt())
    }
}

sealed class BackupResult {
    data class Success(val message: String) : BackupResult()
    data class Error(val message: String) : BackupResult()
}

data class BackupInfo(
    val name: String,
    val size: Long,
    val createdAt: Long,
    val path: String
) 