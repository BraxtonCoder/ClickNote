package com.example.clicknote.data.service

import android.content.Context
import android.net.Uri
import com.example.clicknote.data.storage.CloudStorageAdapter
import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.domain.model.BackupType
import com.example.clicknote.domain.service.BackupService
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.FolderRepository
import com.example.clicknote.domain.repository.UserRepository
import com.example.clicknote.util.BackupEncryption
import com.example.clicknote.util.calculateChecksum
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.*
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val userRepository: UserRepository,
    private val cloudStorageAdapter: CloudStorageAdapter,
    private val backupEncryption: BackupEncryption
) : BackupService {

    private val _backupProgress = MutableStateFlow(0f)
    private val _restoreProgress = MutableStateFlow(0f)
    private val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
    private val tempDir = File(context.cacheDir, "backup_temp").apply { mkdirs() }

    override suspend fun createBackup(): Result<BackupInfo> = runCatching {
        _backupProgress.value = 0f
        
        val notes = noteRepository.getAllNotes().getOrThrow()
        val timestamp = System.currentTimeMillis()
        val backupFile = File(tempDir, "backup_$timestamp.zip")
        val encryptedFile = File(tempDir, "backup_$timestamp.encrypted")
        
        try {
            withContext(Dispatchers.IO) {
                val totalBytes = calculateTotalBytes(notes)
                var bytesWritten = 0L

                ZipOutputStream(BufferedOutputStream(FileOutputStream(backupFile))).use { zip ->
                    // Backup notes
                    val notesJson = com.google.gson.Gson().toJson(notes)
                    zip.putNextEntry(ZipEntry("notes.json"))
                    val notesBytes = notesJson.toByteArray()
                    zip.write(notesBytes)
                    zip.closeEntry()
                    bytesWritten += notesBytes.size
                    _backupProgress.value = bytesWritten.toFloat() / totalBytes

                    // Backup folders
                    val folders = folderRepository.getAllFolders().getOrThrow()
                    val foldersJson = com.google.gson.Gson().toJson(folders)
                    zip.putNextEntry(ZipEntry("folders.json"))
                    val foldersBytes = foldersJson.toByteArray()
                    zip.write(foldersBytes)
                    zip.closeEntry()
                    bytesWritten += foldersBytes.size
                    _backupProgress.value = bytesWritten.toFloat() / totalBytes
                }
            }

            // Encrypt backup
            backupEncryption.encryptFile(backupFile, encryptedFile)
            backupFile.delete()

            // Upload to cloud
            val userId = userRepository.getUserId() ?: throw IllegalStateException("User not logged in")
            val remotePath = "backups/$userId/backup_$timestamp.encrypted"
            val url = cloudStorageAdapter.uploadFile(encryptedFile, remotePath)
            encryptedFile.delete()
            
            BackupInfo(
                id = "backup_$timestamp",
                size = encryptedFile.length(),
                createdAt = LocalDateTime.now(),
                backupType = BackupType.FULL,
                metadata = mapOf(
                    "url" to url,
                    "timestamp" to timestamp.toString(),
                    "noteCount" to notes.size.toString()
                )
            )
        } finally {
            _backupProgress.value = 0f
            backupFile.delete()
            encryptedFile.delete()
        }
    }

    override suspend fun restoreBackup(backupId: String): Result<Unit> = runCatching {
        _restoreProgress.value = 0f
        
        try {
            val userId = userRepository.getUserId() ?: throw IllegalStateException("User not logged in")
            val remotePath = "backups/$userId/$backupId.encrypted"
            val tempFile = File(tempDir, "$backupId.encrypted")
            val decryptedFile = File(tempDir, "$backupId.zip")

            // Download from cloud
            cloudStorageAdapter.downloadFile(remotePath, tempFile)

            // Decrypt backup
            backupEncryption.decryptFile(tempFile, decryptedFile)
            tempFile.delete()

            withContext(Dispatchers.IO) {
                val fileSize = decryptedFile.length()
                var bytesRead = 0L

                ZipInputStream(BufferedInputStream(FileInputStream(decryptedFile))).use { zip ->
                    var entry = zip.nextEntry
                    while (entry != null) {
                        when (entry.name) {
                            "notes.json" -> {
                                val notesJson = zip.readBytes().decodeToString()
                                val notes = com.google.gson.Gson().fromJson(notesJson, Array<com.example.clicknote.domain.model.Note>::class.java)
                                noteRepository.insertNotes(notes.toList()).getOrThrow()
                                bytesRead += entry.size
                                _restoreProgress.value = bytesRead.toFloat() / fileSize
                            }
                            "folders.json" -> {
                                val foldersJson = zip.readBytes().decodeToString()
                                val folders = com.google.gson.Gson().fromJson(foldersJson, Array<com.example.clicknote.domain.model.Folder>::class.java)
                                folderRepository.insertFolders(folders.toList()).getOrThrow()
                                bytesRead += entry.size
                                _restoreProgress.value = bytesRead.toFloat() / fileSize
                            }
                        }
                        entry = zip.nextEntry
                    }
                }
            }
            Unit
        } finally {
            _restoreProgress.value = 0f
        }
    }

    override suspend fun listBackups(): List<BackupInfo> = withContext(Dispatchers.IO) {
        val userId = userRepository.getUserId() ?: return@withContext emptyList()
        val backups = cloudStorageAdapter.listFiles("backups/$userId")
        
        backups.map { file ->
            val timestamp = file.name.substringAfter("backup_").substringBefore(".encrypted").toLong()
            BackupInfo(
                id = file.name.removeSuffix(".encrypted"),
                size = file.size,
                createdAt = LocalDateTime.ofEpochSecond(timestamp / 1000, 0, ZoneOffset.UTC),
                backupType = BackupType.FULL,
                metadata = mapOf(
                    "path" to file.path,
                    "timestamp" to timestamp.toString()
                )
            )
        }.sortedByDescending { it.createdAt }
    }

    override suspend fun deleteBackup(backupId: String): Result<Unit> = runCatching {
        val userId = userRepository.getUserId() ?: throw IllegalStateException("User not logged in")
        cloudStorageAdapter.deleteFile("backups/$userId/$backupId.encrypted")
    }

    override suspend fun scheduleAutomaticBackup(intervalHours: Int): Result<Unit> = runCatching {
        // TODO: Implement using WorkManager
        Unit
    }

    override suspend fun cancelAutomaticBackup(): Result<Unit> = runCatching {
        // TODO: Implement using WorkManager
        Unit
    }

    override fun observeBackupProgress(): Flow<Float> = _backupProgress.asStateFlow()

    override fun observeRestoreProgress(): Flow<Float> = _restoreProgress.asStateFlow()

    override suspend fun validateBackup(backupId: String): Result<Boolean> = runCatching {
        val userId = userRepository.getUserId() ?: throw IllegalStateException("User not logged in")
        cloudStorageAdapter.fileExists("backups/$userId/$backupId.encrypted")
    }

    override suspend fun getLastBackupTime(): Result<Long> = runCatching {
        val backups = listBackups()
        backups.maxOfOrNull { it.createdAt.toEpochSecond(ZoneOffset.UTC) * 1000 } ?: 0L
    }

    private suspend fun calculateTotalBytes(notes: List<com.example.clicknote.domain.model.Note>): Long {
        val notesJson = com.google.gson.Gson().toJson(notes)
        val folders = folderRepository.getAllFolders().getOrThrow()
        val foldersJson = com.google.gson.Gson().toJson(folders)
        return notesJson.toByteArray().size.toLong() + foldersJson.toByteArray().size.toLong()
    }
} 