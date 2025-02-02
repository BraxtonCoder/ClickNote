package com.example.clicknote.data.storage

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import java.io.File
import java.io.InputStream
import java.nio.file.Files
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleCloudStorageAdapter @Inject constructor(
    private val storage: Storage,
    private val bucketName: String
) : CloudStorageAdapter {

    override suspend fun uploadFile(localFile: File, remotePath: String): String {
        val blobId = BlobId.of(bucketName, remotePath)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType(Files.probeContentType(localFile.toPath()))
            .build()
        
        storage.create(blobInfo, Files.readAllBytes(localFile.toPath()))
        return storage.get(blobId).signUrl(3600).toString()
    }

    override suspend fun downloadFile(remotePath: String, localFile: File) {
        val blob = storage.get(BlobId.of(bucketName, remotePath))
        blob.downloadTo(localFile.toPath())
    }

    override suspend fun deleteFile(remotePath: String) {
        storage.delete(BlobId.of(bucketName, remotePath))
    }

    override suspend fun getFileUrl(remotePath: String): String {
        val blob = storage.get(BlobId.of(bucketName, remotePath))
        return blob.signUrl(3600).toString()
    }

    override suspend fun listFiles(remotePath: String): List<CloudFile> {
        val blobs = storage.list(
            bucketName,
            Storage.BlobListOption.prefix(remotePath),
            Storage.BlobListOption.currentDirectory()
        )

        return buildList {
            blobs.iterateAll().forEach { blob ->
                if (blob.name != remotePath) {
                    add(CloudFile(
                        name = blob.name.substringAfterLast('/'),
                        path = blob.name,
                        size = blob.size,
                        lastModified = blob.updateTime,
                        isDirectory = blob.name.endsWith("/"),
                        metadata = blob.toCloudFileMetadata()
                    ))
                }
            }
        }
    }

    override suspend fun getFileMetadata(remotePath: String): CloudFileMetadata {
        val blob = storage.get(BlobId.of(bucketName, remotePath))
        return blob.toCloudFileMetadata()
    }

    override suspend fun getStorageUsage(): Long {
        var totalSize = 0L
        storage.list(bucketName).iterateAll().forEach { blob ->
            totalSize += blob.size
        }
        return totalSize
    }

    override suspend fun getStorageLimit(): Long {
        // Google Cloud Storage has no built-in storage limit per bucket
        // Return a configured limit or max value
        return Long.MAX_VALUE
    }

    override suspend fun createDirectory(remotePath: String) {
        // Google Cloud Storage doesn't have real directories
        // Create an empty object with a trailing slash
        val path = if (remotePath.endsWith("/")) remotePath else "$remotePath/"
        val blobId = BlobId.of(bucketName, path)
        val blobInfo = BlobInfo.newBuilder(blobId)
            .setContentType("application/directory")
            .build()
        storage.create(blobInfo, ByteArray(0))
    }

    override suspend fun deleteDirectory(remotePath: String) {
        val path = if (remotePath.endsWith("/")) remotePath else "$remotePath/"
        storage.list(bucketName, Storage.BlobListOption.prefix(path))
            .iterateAll()
            .forEach { blob ->
                storage.delete(blob.blobId)
            }
    }

    override suspend fun moveFile(sourcePath: String, destinationPath: String) {
        copyFile(sourcePath, destinationPath)
        deleteFile(sourcePath)
    }

    override suspend fun copyFile(sourcePath: String, destinationPath: String) {
        val sourceBlob = storage.get(BlobId.of(bucketName, sourcePath))
        val destBlobId = BlobId.of(bucketName, destinationPath)
        storage.copy(Storage.CopyRequest.of(sourceBlob.blobId, destBlobId))
    }

    override suspend fun getFileInputStream(remotePath: String): InputStream {
        val blob = storage.get(BlobId.of(bucketName, remotePath))
        return blob.reader()
    }

    override suspend fun fileExists(remotePath: String): Boolean {
        return storage.get(BlobId.of(bucketName, remotePath))?.exists() ?: false
    }

    private fun com.google.cloud.storage.Blob.toCloudFileMetadata() = CloudFileMetadata(
        contentType = contentType ?: "application/octet-stream",
        eTag = etag,
        customMetadata = metadata ?: emptyMap(),
        createdAt = createTime,
        updatedAt = updateTime,
        owner = owner?.email
    )
} 