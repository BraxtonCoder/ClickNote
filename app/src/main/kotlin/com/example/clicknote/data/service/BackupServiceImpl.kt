package com.example.clicknote.data.service

import android.content.Context
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.service.BackupService
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.FolderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BackupServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository,
    private val preferences: UserPreferencesDataStore
) : BackupService {

    private val _backupProgress = MutableStateFlow(0f)
    private val _restoreProgress = MutableStateFlow(0f)
    private val backupDir = File(context.filesDir, "backups")

    init {
        backupDir.mkdirs()
    }

    override suspend fun createBackup(): Result<BackupInfo> = runCatching {
        _backupProgress.value = 0f
        
        val notes = noteRepository.getAllNotes().getOrThrow()
        val backupFile = File(backupDir, "backup_${System.currentTimeMillis()}.zip")
        
        try {
            ZipOutputStream(FileOutputStream(backupFile)).use { zip ->
                // Backup notes
                val notesJson = com.google.gson.Gson().toJson(notes)
                zip.putNextEntry(ZipEntry("notes.json"))
                zip.write(notesJson.toByteArray())
                zip.closeEntry()
                _backupProgress.value = 0.5f

                // Backup folders
                val folders = folderRepository.getAllFolders().getOrThrow()
                val foldersJson = com.google.gson.Gson().toJson(folders)
                zip.putNextEntry(ZipEntry("folders.json"))
                zip.write(foldersJson.toByteArray())
                zip.closeEntry()
                _backupProgress.value = 1.0f
            }
            
            val backupInfo = BackupInfo(
                id = backupFile.name,
                size = backupFile.length(),
                createdAt = LocalDateTime.now(),
                noteCount = notes.size,
                audioCount = 0, // TODO: Get actual count
                compressionLevel = CompressionLevel.LOW,
                isEncrypted = false,
                cloudStorageProvider = CloudStorageProvider.NONE,
                metadata = emptyMap()
            )
            
            _backupProgress.value = 0f
            backupInfo
        } finally {
            _backupProgress.value = 0f
        }
    }

    override suspend fun restoreBackup(backupId: String): Result<Unit> = runCatching {
        _restoreProgress.value = 0f
        
        try {
            val backupFile = File(context.filesDir, backupId)
            if (!backupFile.exists()) {
                throw IllegalArgumentException("Backup file not found")
            }
            
            ZipInputStream(FileInputStream(backupFile)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "notes.json" -> {
                            val notesJson = zip.readBytes().decodeToString()
                            val notes = com.google.gson.Gson().fromJson(notesJson, Array<com.example.clicknote.domain.model.Note>::class.java)
                            noteRepository.insertNotes(notes.toList()).getOrThrow()
                            _restoreProgress.value = 0.5f
                        }
                        "folders.json" -> {
                            val foldersJson = zip.readBytes().decodeToString()
                            val folders = com.google.gson.Gson().fromJson(foldersJson, Array<com.example.clicknote.domain.model.Folder>::class.java)
                            folderRepository.insertFolders(folders.toList()).getOrThrow()
                            _restoreProgress.value = 1.0f
                        }
                    }
                    entry = zip.nextEntry
                }
            }
            _restoreProgress.value = 0f
            Unit
        } finally {
            _restoreProgress.value = 0f
        }
    }

    override suspend fun listBackups(): List<BackupInfo> {
        return context.filesDir.listFiles { file ->
            file.name.startsWith("backup_") && file.name.endsWith(".zip")
        }?.map { file ->
            BackupInfo(
                id = file.name,
                size = file.length(),
                createdAt = LocalDateTime.now(), // TODO: Get actual creation time
                noteCount = 0, // TODO: Get actual count
                audioCount = 0, // TODO: Get actual count
                compressionLevel = CompressionLevel.LOW,
                isEncrypted = false,
                cloudStorageProvider = CloudStorageProvider.NONE,
                metadata = emptyMap()
            )
        } ?: emptyList()
    }

    override suspend fun deleteBackup(backupId: String): Result<Unit> = runCatching {
        val backupFile = File(context.filesDir, backupId)
        if (backupFile.exists()) {
            backupFile.delete()
        }
        Unit
    }

    override suspend fun scheduleAutomaticBackup(intervalHours: Int): Result<Unit> = runCatching {
        // TODO: Implement automatic backup scheduling
        Unit
    }

    override suspend fun cancelAutomaticBackup(): Result<Unit> = runCatching {
        // TODO: Implement automatic backup cancellation
        Unit
    }

    override fun observeBackupProgress(): Flow<Float> = _backupProgress.asStateFlow()

    override fun observeRestoreProgress(): Flow<Float> = _restoreProgress.asStateFlow()

    override suspend fun validateBackup(backupId: String): Result<Boolean> = runCatching {
        val backupFile = File(context.filesDir, backupId)
        backupFile.exists() && backupFile.length() > 0
    }

    override suspend fun getLastBackupTime(): Result<Long> = runCatching {
        val backups = listBackups()
        backups.maxOfOrNull { it.createdAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000 }
            ?: 0L
    }
} 