package com.example.clicknote.service

import android.content.Context
import com.example.clicknote.domain.model.*
import com.example.clicknote.util.calculateChecksum
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.*
import java.time.LocalDateTime
import java.util.zip.Deflater
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DifferentialBackupService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val backupService: BackupService
) {
    private val tempDir = File(context.cacheDir, "diff_backup_temp")
    private val manifestFile = File(tempDir, "manifest.json")

    init {
        tempDir.mkdirs()
    }

    suspend fun createDifferentialBackup(
        name: String,
        compressionLevel: CompressionLevel,
        onProgress: (Int) -> Unit
    ): BackupResult {
        try {
            // Get the latest backup version
            val latestBackup = backupService.listBackups()
                .maxByOrNull { it.createdAt }

            // Create a manifest of current files with checksums
            val currentManifest = createFileManifest()

            // Compare with previous backup if available
            val changedFiles = if (latestBackup != null) {
                val previousManifest = loadPreviousManifest(latestBackup)
                findChangedFiles(currentManifest, previousManifest)
            } else {
                currentManifest.map { it.file }
            }

            // Create differential backup
            val backupFile = createCompressedBackup(
                changedFiles,
                name,
                compressionLevel,
                onProgress
            )

            // Save new manifest
            saveManifest(currentManifest)

            return BackupResult.Success("Differential backup created successfully")
        } catch (e: Exception) {
            return BackupResult.Error(e.message ?: "Differential backup failed")
        }
    }

    private fun createFileManifest(): List<FileManifestEntry> {
        val entries = mutableListOf<FileManifestEntry>()
        
        // Add database
        val dbFile = context.getDatabasePath("clicknote.db")
        entries.add(FileManifestEntry(
            file = dbFile,
            path = "database/clicknote.db",
            checksum = calculateChecksum(dbFile),
            lastModified = LocalDateTime.now()
        ))

        // Add audio files
        val audioDir = File(context.filesDir, "audio")
        if (audioDir.exists()) {
            audioDir.walk().forEach { file ->
                if (file.isFile) {
                    entries.add(FileManifestEntry(
                        file = file,
                        path = "audio/${file.name}",
                        checksum = calculateChecksum(file),
                        lastModified = LocalDateTime.now()
                    ))
                }
            }
        }

        return entries
    }

    private fun createCompressedBackup(
        files: List<File>,
        name: String,
        compressionLevel: CompressionLevel,
        onProgress: (Int) -> Unit
    ): File {
        val backupFile = File(tempDir, "$name.zip")
        val totalSize = files.sumOf { it.length() }
        var processedSize = 0L

        ZipOutputStream(backupFile.outputStream()).use { zip ->
            zip.setLevel(compressionLevel.level)
            
            files.forEach { file ->
                val entry = ZipEntry(file.name)
                zip.putNextEntry(entry)
                
                file.inputStream().use { input ->
                    val buffer = ByteArray(8192)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        zip.write(buffer, 0, bytesRead)
                        processedSize += bytesRead
                        onProgress((processedSize * 100 / totalSize).toInt())
                    }
                }
                
                zip.closeEntry()
            }
        }

        return backupFile
    }

    private fun findChangedFiles(
        currentManifest: List<FileManifestEntry>,
        previousManifest: List<FileManifestEntry>
    ): List<File> {
        return currentManifest.filter { current ->
            val previous = previousManifest.find { it.path == current.path }
            previous == null || previous.checksum != current.checksum
        }.map { it.file }
    }

    private fun loadPreviousManifest(backup: BackupInfo): List<FileManifestEntry> {
        // Implementation to load previous manifest from backup
        return emptyList() // Placeholder
    }

    private fun saveManifest(manifest: List<FileManifestEntry>) {
        // Implementation to save current manifest
    }

    data class FileManifestEntry(
        val file: File,
        val path: String,
        val checksum: String,
        val lastModified: LocalDateTime
    )
} 