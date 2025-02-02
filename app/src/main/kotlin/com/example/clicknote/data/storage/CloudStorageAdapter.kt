package com.example.clicknote.data.storage

import java.io.File
import java.io.InputStream

interface CloudStorageAdapter {
    suspend fun uploadFile(localFile: File, remotePath: String): String
    suspend fun downloadFile(remotePath: String, localFile: File)
    suspend fun deleteFile(remotePath: String)
    suspend fun getFileUrl(remotePath: String): String
    suspend fun listFiles(remotePath: String): List<CloudFile>
    suspend fun getFileMetadata(remotePath: String): CloudFileMetadata
    suspend fun getStorageUsage(): Long
    suspend fun getStorageLimit(): Long
    suspend fun createDirectory(remotePath: String)
    suspend fun deleteDirectory(remotePath: String)
    suspend fun moveFile(sourcePath: String, destinationPath: String)
    suspend fun copyFile(sourcePath: String, destinationPath: String)
    suspend fun getFileInputStream(remotePath: String): InputStream
    suspend fun fileExists(remotePath: String): Boolean
}

data class CloudFile(
    val name: String,
    val path: String,
    val size: Long,
    val lastModified: Long,
    val isDirectory: Boolean,
    val metadata: CloudFileMetadata
)

data class CloudFileMetadata(
    val contentType: String,
    val eTag: String?,
    val customMetadata: Map<String, String>,
    val createdAt: Long,
    val updatedAt: Long,
    val owner: String?
) 