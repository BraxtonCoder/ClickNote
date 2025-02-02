package com.example.clicknote.domain.service

import android.net.Uri
import kotlinx.coroutines.flow.Flow
import java.io.File

interface CloudStorageService {
    suspend fun uploadFile(userId: String, path: String, file: File): Result<String>
    suspend fun uploadFile(userId: String, path: String, uri: Uri): Result<String>
    suspend fun downloadFile(userId: String, path: String): Result<File>
    suspend fun deleteFile(userId: String, path: String): Result<Unit>
    suspend fun getFileMetadata(userId: String, path: String): Result<FileMetadata>
    suspend fun listFiles(userId: String, path: String): Result<List<FileMetadata>>
    suspend fun getStorageUsage(userId: String): Result<Long>
    fun getUploadProgress(): Flow<Float>
    fun getDownloadProgress(): Flow<Float>
    fun cancelOperation()
    suspend fun isOperationInProgress(): Boolean
}

data class FileMetadata(
    val name: String,
    val path: String,
    val size: Long,
    val mimeType: String,
    val createdAt: Long,
    val updatedAt: Long,
    val downloadUrl: String?
) 