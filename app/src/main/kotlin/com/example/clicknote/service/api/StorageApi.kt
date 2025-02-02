package com.example.clicknote.service.api

import java.io.File
import kotlinx.coroutines.flow.Flow

interface StorageApi {
    suspend fun uploadFile(file: File, path: String): Result<String>
    suspend fun downloadFile(path: String, destination: File): Result<File>
    suspend fun deleteFile(path: String): Result<Unit>
    suspend fun listFiles(path: String): Result<List<StorageFile>>
    suspend fun getFileMetadata(path: String): Result<StorageFileMetadata>
    suspend fun getStorageUsage(): Result<StorageUsage>
    fun getUploadProgress(): Flow<Float>
    fun getDownloadProgress(): Flow<Float>
}

data class StorageFile(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val metadata: Map<String, String> = emptyMap()
)

data class StorageFileMetadata(
    val size: Long,
    val lastModified: Long,
    val contentType: String?,
    val metadata: Map<String, String> = emptyMap()
)

data class StorageUsage(
    val used: Long,
    val total: Long,
    val available: Long
) 