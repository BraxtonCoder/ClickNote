package com.example.clicknote.service.storage

import kotlinx.coroutines.flow.Flow
import java.io.File

interface StorageService {
    suspend fun saveFile(file: File, path: String): String
    suspend fun getFile(path: String): File
    suspend fun deleteFile(path: String)
    suspend fun listFiles(path: String): List<StorageFile>
    suspend fun searchFiles(query: String): List<StorageFile>
    fun observeStorageChanges(): Flow<StorageEvent>
    suspend fun syncFiles()
    suspend fun backup()
    suspend fun restore()
}

data class StorageFile(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val type: StorageFileType
)

enum class StorageFileType {
    AUDIO,
    TRANSCRIPTION,
    BACKUP,
    OTHER
}

sealed class StorageEvent {
    data class FileAdded(val file: StorageFile) : StorageEvent()
    data class FileModified(val file: StorageFile) : StorageEvent()
    data class FileDeleted(val path: String) : StorageEvent()
    data class SyncStarted(val totalFiles: Int) : StorageEvent()
    data class SyncProgress(val progress: Int, val total: Int) : StorageEvent()
    data class SyncCompleted(val successCount: Int, val failureCount: Int) : StorageEvent()
    data class Error(val message: String, val cause: Throwable? = null) : StorageEvent()
} 