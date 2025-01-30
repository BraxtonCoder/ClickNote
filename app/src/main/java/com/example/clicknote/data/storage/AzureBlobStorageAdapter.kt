package com.example.clicknote.data.storage

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.BlobListDetails
import com.azure.storage.blob.models.ListBlobsOptions
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AzureBlobStorageAdapter @Inject constructor(
    private val blobServiceClient: BlobServiceClient,
    private val containerName: String
) : CloudStorageAdapter {
    private val containerClient: BlobContainerClient = blobServiceClient.getBlobContainerClient(containerName)

    override suspend fun uploadFile(localFile: File, remotePath: String): String {
        val blobClient = containerClient.getBlobClient(remotePath)
        blobClient.uploadFromFile(localFile.absolutePath)
        return blobClient.blobUrl
    }

    override suspend fun downloadFile(remotePath: String, localFile: File) {
        val blobClient = containerClient.getBlobClient(remotePath)
        blobClient.downloadToFile(localFile.absolutePath, true)
    }

    override suspend fun deleteFile(remotePath: String) {
        val blobClient = containerClient.getBlobClient(remotePath)
        blobClient.delete()
    }

    override suspend fun getFileUrl(remotePath: String): String {
        val blobClient = containerClient.getBlobClient(remotePath)
        return blobClient.blobUrl
    }

    override suspend fun listFiles(remotePath: String): List<CloudFile> {
        val options = ListBlobsOptions()
            .setPrefix(remotePath)
            .setDetails(BlobListDetails().setMetadata(true))

        return containerClient.listBlobs(options, null).map { blob ->
            CloudFile(
                name = blob.name.substringAfterLast('/'),
                path = blob.name,
                size = blob.properties.contentLength ?: 0,
                lastModified = blob.properties.lastModified?.toEpochSecond() ?: 0,
                isDirectory = blob.name.endsWith("/"),
                metadata = getFileMetadata(blob.name)
            )
        }.toList()
    }

    override suspend fun getFileMetadata(remotePath: String): CloudFileMetadata {
        val blobClient = containerClient.getBlobClient(remotePath)
        val properties = blobClient.properties
        return CloudFileMetadata(
            contentType = properties.contentType ?: "application/octet-stream",
            eTag = properties.eTag,
            customMetadata = blobClient.getProperties().metadata ?: emptyMap(),
            createdAt = properties.creationTime?.toEpochSecond() ?: 0,
            updatedAt = properties.lastModified?.toEpochSecond() ?: 0,
            owner = null // Azure Blob Storage doesn't provide owner information
        )
    }

    override suspend fun getStorageUsage(): Long {
        var totalSize = 0L
        containerClient.listBlobs().forEach { blob ->
            totalSize += blob.properties.contentLength ?: 0
        }
        return totalSize
    }

    override suspend fun getStorageLimit(): Long {
        // Azure Blob Storage has no built-in storage limit per container
        // Return a configured limit or max value
        return Long.MAX_VALUE
    }

    override suspend fun createDirectory(remotePath: String) {
        // Azure Blob Storage doesn't have real directories
        // Create an empty blob with a trailing slash
        val path = if (remotePath.endsWith("/")) remotePath else "$remotePath/"
        val blobClient = containerClient.getBlobClient(path)
        blobClient.upload(ByteArray(0).inputStream(), 0)
    }

    override suspend fun deleteDirectory(remotePath: String) {
        val path = if (remotePath.endsWith("/")) remotePath else "$remotePath/"
        containerClient.listBlobs(ListBlobsOptions().setPrefix(path), null)
            .forEach { blob ->
                containerClient.getBlobClient(blob.name).delete()
            }
    }

    override suspend fun moveFile(sourcePath: String, destinationPath: String) {
        copyFile(sourcePath, destinationPath)
        deleteFile(sourcePath)
    }

    override suspend fun copyFile(sourcePath: String, destinationPath: String) {
        val sourceBlob = containerClient.getBlobClient(sourcePath)
        val destinationBlob = containerClient.getBlobClient(destinationPath)
        destinationBlob.beginCopy(sourceBlob.blobUrl).waitForCompletion()
    }

    override suspend fun getFileInputStream(remotePath: String): InputStream {
        val blobClient = containerClient.getBlobClient(remotePath)
        return blobClient.openInputStream()
    }

    override suspend fun fileExists(remotePath: String): Boolean {
        val blobClient = containerClient.getBlobClient(remotePath)
        return blobClient.exists()
    }
} 