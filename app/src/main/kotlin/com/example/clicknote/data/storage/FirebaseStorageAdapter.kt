package com.example.clicknote.data.storage

import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseStorageAdapter @Inject constructor(
    private val storage: FirebaseStorage
) : CloudStorageAdapter {

    override suspend fun uploadFile(localFile: File, remotePath: String): String {
        val ref = storage.reference.child(remotePath)
        val metadata = StorageMetadata.Builder()
            .setContentType(getContentType(localFile))
            .build()
        
        return ref.putFile(android.net.Uri.fromFile(localFile), metadata)
            .await()
            .storage
            .downloadUrl
            .await()
            .toString()
    }

    override suspend fun downloadFile(remotePath: String, localFile: File) {
        val ref = storage.reference.child(remotePath)
        ref.getFile(localFile).await()
    }

    override suspend fun deleteFile(remotePath: String) {
        val ref = storage.reference.child(remotePath)
        ref.delete().await()
    }

    override suspend fun getFileUrl(remotePath: String): String {
        val ref = storage.reference.child(remotePath)
        return ref.downloadUrl.await().toString()
    }

    override suspend fun listFiles(remotePath: String): List<CloudFile> {
        val ref = storage.reference.child(remotePath)
        val result = ref.listAll().await()
        
        return buildList {
            result.items.forEach { item ->
                val metadata = item.metadata.await()
                add(CloudFile(
                    name = item.name,
                    path = item.path,
                    size = metadata.sizeBytes,
                    lastModified = metadata.updatedTimeMillis,
                    isDirectory = false,
                    metadata = metadata.toCloudFileMetadata()
                ))
            }
            
            result.prefixes.forEach { prefix ->
                add(CloudFile(
                    name = prefix.name,
                    path = prefix.path,
                    size = 0,
                    lastModified = 0,
                    isDirectory = true,
                    metadata = CloudFileMetadata(
                        contentType = "application/directory",
                        eTag = null,
                        customMetadata = emptyMap(),
                        createdAt = 0,
                        updatedAt = 0,
                        owner = null
                    )
                ))
            }
        }
    }

    override suspend fun getFileMetadata(remotePath: String): CloudFileMetadata {
        val ref = storage.reference.child(remotePath)
        val metadata = ref.metadata.await()
        return metadata.toCloudFileMetadata()
    }

    override suspend fun getStorageUsage(): Long {
        // Firebase doesn't provide direct storage usage API
        // Implement custom tracking or use a different service for usage metrics
        return 0L
    }

    override suspend fun getStorageLimit(): Long {
        // Firebase doesn't provide storage limit API
        // Return a default value or fetch from your backend
        return 5L * 1024 * 1024 * 1024 // 5GB default
    }

    override suspend fun createDirectory(remotePath: String) {
        // Firebase Storage doesn't have explicit directories
        // They are created implicitly when files are uploaded
    }

    override suspend fun deleteDirectory(remotePath: String) {
        val ref = storage.reference.child(remotePath)
        val result = ref.listAll().await()
        
        result.items.forEach { item ->
            item.delete().await()
        }
        
        result.prefixes.forEach { prefix ->
            deleteDirectory(prefix.path)
        }
    }

    override suspend fun moveFile(sourcePath: String, destinationPath: String) {
        // Firebase doesn't support moving files directly
        // Copy and delete instead
        copyFile(sourcePath, destinationPath)
        deleteFile(sourcePath)
    }

    override suspend fun copyFile(sourcePath: String, destinationPath: String) {
        val sourceRef = storage.reference.child(sourcePath)
        val destRef = storage.reference.child(destinationPath)
        
        val metadata = sourceRef.metadata.await()
        val stream = sourceRef.stream.await()
        
        destRef.putStream(stream.stream, metadata).await()
    }

    override suspend fun getFileInputStream(remotePath: String): InputStream {
        val ref = storage.reference.child(remotePath)
        return ref.stream.await().stream
    }

    override suspend fun fileExists(remotePath: String): Boolean {
        return try {
            val ref = storage.reference.child(remotePath)
            ref.metadata.await()
            true
        } catch (e: Exception) {
            false
        }
    }

    private fun getContentType(file: File): String {
        return when (file.extension.lowercase()) {
            "txt" -> "text/plain"
            "json" -> "application/json"
            "mp3" -> "audio/mpeg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/mp4"
            "aac" -> "audio/aac"
            else -> "application/octet-stream"
        }
    }

    private fun StorageMetadata.toCloudFileMetadata() = CloudFileMetadata(
        contentType = contentType ?: "application/octet-stream",
        eTag = md5Hash,
        customMetadata = customMetadataKeys?.associateWith { getCustomMetadata(it) ?: "" } ?: emptyMap(),
        createdAt = creationTimeMillis,
        updatedAt = updatedTimeMillis,
        owner = getCustomMetadata("owner")
    )
} 