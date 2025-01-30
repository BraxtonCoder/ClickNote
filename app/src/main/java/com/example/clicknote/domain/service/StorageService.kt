package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow
import java.io.File

interface StorageService {
    suspend fun saveFile(file: File, path: String): Result<String>
    suspend fun getFile(path: String): Result<File>
    suspend fun deleteFile(path: String): Result<Unit>
    suspend fun listFiles(directory: String): Result<List<String>>
    suspend fun getStorageUsage(): Long
    suspend fun getFreeSpace(): Long
    suspend fun getTotalSpace(): Long
    fun getStorageStats(): Flow<StorageStats>
    suspend fun cleanupOldFiles(olderThan: Long): Result<Int>
    suspend fun backupFiles(targetPath: String): Result<Unit>
    suspend fun restoreFiles(sourcePath: String): Result<Unit>
}

data class StorageStats(
    val usedSpace: Long,
    val freeSpace: Long,
    val totalSpace: Long,
    val audioFiles: Int,
    val transcriptionFiles: Int
) 