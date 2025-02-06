package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.BackupInfo
import com.example.clicknote.domain.model.BackupResult
import com.example.clicknote.domain.model.BackupType
import com.example.clicknote.domain.service.BackupService
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.FolderRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.io.File
import java.time.LocalDateTime
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DifferentialBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupService: BackupService,
    private val noteRepository: NoteRepository,
    private val folderRepository: FolderRepository
) {
    private val backupDir = File(context.filesDir, "backups").apply { mkdirs() }
    private val tempDir = File(context.cacheDir, "backup_temp").apply { mkdirs() }

    suspend fun createDifferentialBackup(): Flow<BackupResult> = flow {
        try {
            // Get latest full backup
            val latestBackup = backupService.listBackups()
                .filter { it.backupType == BackupType.FULL }
                .maxByOrNull { it.createdAt }

            if (latestBackup == null) {
                emit(BackupResult.Error("No full backup found to create differential backup"))
                return@flow
            }

            // Get changed files since last backup
            val changedFiles = getChangedFilesSinceBackup(latestBackup)
            if (changedFiles.isEmpty()) {
                emit(BackupResult.Success(
                    backup = latestBackup,
                    changedFiles = 0,
                    totalSize = 0L,
                    compressionRatio = 0f
                ))
                return@flow
            }

            // Create differential backup
            val timestamp = System.currentTimeMillis()
            val backupFile = File(tempDir, "backup_${timestamp}.zip")

            withContext(Dispatchers.IO) {
                var processedFiles = 0
                var totalSize = 0L
                var uncompressedSize = 0L

                ZipOutputStream(backupFile.outputStream().buffered()).use { zip ->
                    changedFiles.forEach { file ->
                        emit(BackupResult.Progress(
                            percentage = processedFiles.toFloat() / changedFiles.size,
                            currentFile = file.name,
                            processedFiles = processedFiles,
                            totalFiles = changedFiles.size
                        ))

                        zip.putNextEntry(ZipEntry(file.name))
                        file.inputStream().buffered().use { input ->
                            input.copyTo(zip)
                            uncompressedSize += file.length()
                        }
                        zip.closeEntry()

                        processedFiles++
                        totalSize += file.length()
                    }
                }

                val compressionRatio = 1 - (backupFile.length().toFloat() / uncompressedSize)

                val backupInfo = BackupInfo(
                    id = "diff_${timestamp}",
                    size = backupFile.length(),
                    createdAt = LocalDateTime.now(),
                    backupType = BackupType.DIFFERENTIAL,
                    metadata = mapOf(
                        "baseBackupId" to latestBackup.id,
                        "changedFiles" to changedFiles.size.toString(),
                        "compressionRatio" to compressionRatio.toString()
                    )
                )

                emit(BackupResult.Success(
                    backup = backupInfo,
                    changedFiles = changedFiles.size,
                    totalSize = totalSize,
                    compressionRatio = compressionRatio
                ))
            }

        } catch (e: Exception) {
            emit(BackupResult.Error("Failed to create differential backup", e))
        } finally {
            tempDir.listFiles()?.forEach { it.delete() }
        }
    }

    private suspend fun getChangedFilesSinceBackup(baseBackup: BackupInfo): List<File> {
        val changedFiles = mutableListOf<File>()

        // Check notes
        val notes = noteRepository.getAllNotes().getOrThrow()
        notes.filter { it.modifiedAt.isAfter(baseBackup.createdAt) }
            .forEach { note ->
                note.audioPath?.let { path ->
                    val audioFile = File(path)
                    if (audioFile.exists()) {
                        changedFiles.add(audioFile)
                    }
                }
            }

        // Check folders
        val folders = folderRepository.getAllFolders().getOrThrow()
        folders.filter { it.modifiedAt.isAfter(baseBackup.createdAt) }
            .forEach { folder ->
                // Add folder-specific files if any
            }

        return changedFiles
    }
} 