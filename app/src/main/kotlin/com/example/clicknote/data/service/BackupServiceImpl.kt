package com.example.clicknote.data.service

import android.content.Context
import com.example.clicknote.domain.service.BackupService
import com.example.clicknote.domain.preferences.UserPreferences
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.FolderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
    private val userPreferences: UserPreferences
) : BackupService {
    private val backupProgress = MutableStateFlow(0f)
    private val restoreProgress = MutableStateFlow(0f)
    private val backupDir = File(context.filesDir, "backups")

    init {
        backupDir.mkdirs()
    }

    override suspend fun createBackup(): Result<File> = withContext(Dispatchers.IO) {
        try {
            val backupFile = File(backupDir, "backup_${System.currentTimeMillis()}.zip")
            ZipOutputStream(FileOutputStream(backupFile)).use { zip ->
                // Backup notes
                val notes = noteRepository.getAllNotes().getOrThrow()
                val notesJson = com.google.gson.Gson().toJson(notes)
                zip.putNextEntry(ZipEntry("notes.json"))
                zip.write(notesJson.toByteArray())
                zip.closeEntry()
                backupProgress.value = 0.5f

                // Backup folders
                val folders = folderRepository.getAllFolders().getOrThrow()
                val foldersJson = com.google.gson.Gson().toJson(folders)
                zip.putNextEntry(ZipEntry("folders.json"))
                zip.write(foldersJson.toByteArray())
                zip.closeEntry()
                backupProgress.value = 1.0f
            }
            
            userPreferences.setLastBackupTime(System.currentTimeMillis())
            Result.success(backupFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun restoreBackup(backupFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            ZipInputStream(FileInputStream(backupFile)).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    when (entry.name) {
                        "notes.json" -> {
                            val notesJson = zip.readBytes().decodeToString()
                            val notes = com.google.gson.Gson().fromJson(notesJson, Array<com.example.clicknote.domain.model.Note>::class.java)
                            noteRepository.insertNotes(notes.toList()).getOrThrow()
                            restoreProgress.value = 0.5f
                        }
                        "folders.json" -> {
                            val foldersJson = zip.readBytes().decodeToString()
                            val folders = com.google.gson.Gson().fromJson(foldersJson, Array<com.example.clicknote.domain.model.Folder>::class.java)
                            folderRepository.insertFolders(folders.toList()).getOrThrow()
                            restoreProgress.value = 1.0f
                        }
                    }
                    entry = zip.nextEntry
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getBackupFiles(): Result<List<File>> = withContext(Dispatchers.IO) {
        try {
            Result.success(backupDir.listFiles()?.filter { it.extension == "zip" }?.sortedByDescending { it.lastModified() } ?: emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteBackup(backupFile: File): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (backupFile.exists() && backupFile.delete()) {
                Result.success(Unit)
            } else {
                Result.failure(IllegalStateException("Failed to delete backup file"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun scheduleAutomaticBackup(intervalHours: Int): Result<Unit> {
        userPreferences.setAutoBackupInterval(intervalHours)
        userPreferences.setAutoBackupEnabled(true)
        // TODO: Implement automatic backup scheduling using WorkManager
        return Result.success(Unit)
    }

    override suspend fun cancelAutomaticBackup(): Result<Unit> {
        userPreferences.setAutoBackupEnabled(false)
        // TODO: Implement automatic backup cancellation
        return Result.success(Unit)
    }

    override fun observeBackupProgress(): Flow<Float> = backupProgress

    override fun observeRestoreProgress(): Flow<Float> = restoreProgress

    override suspend fun validateBackup(backupFile: File): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            var isValid = true
            ZipInputStream(FileInputStream(backupFile)).use { zip ->
                var entry = zip.nextEntry
                val requiredEntries = setOf("notes.json", "folders.json")
                val foundEntries = mutableSetOf<String>()
                
                while (entry != null) {
                    foundEntries.add(entry.name)
                    entry = zip.nextEntry
                }
                
                isValid = foundEntries.containsAll(requiredEntries)
            }
            Result.success(isValid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getLastBackupTime(): Result<Long> {
        return Result.success(userPreferences.getLastBackupTime())
    }
} 