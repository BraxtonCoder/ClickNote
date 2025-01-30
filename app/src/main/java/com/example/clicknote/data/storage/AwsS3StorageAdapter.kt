package com.example.clicknote.data.storage

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.*
import java.io.File
import java.io.InputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AwsS3StorageAdapter @Inject constructor(
    private val credentials: AWSCredentials,
    private val bucketName: String
) : CloudStorageAdapter {
    private val s3Client = AmazonS3Client(credentials)

    override suspend fun uploadFile(localFile: File, remotePath: String): String {
        val request = PutObjectRequest(bucketName, remotePath, localFile)
            .withCannedAcl(CannedAccessControlList.Private)
        s3Client.putObject(request)
        return s3Client.getUrl(bucketName, remotePath).toString()
    }

    override suspend fun downloadFile(remotePath: String, localFile: File) {
        val request = GetObjectRequest(bucketName, remotePath)
        s3Client.getObject(request, localFile)
    }

    override suspend fun deleteFile(remotePath: String) {
        s3Client.deleteObject(bucketName, remotePath)
    }

    override suspend fun getFileUrl(remotePath: String): String {
        return s3Client.getUrl(bucketName, remotePath).toString()
    }

    override suspend fun listFiles(remotePath: String): List<CloudFile> {
        val request = ListObjectsV2Request()
            .withBucketName(bucketName)
            .withPrefix(remotePath)
            .withDelimiter("/")

        val result = s3Client.listObjectsV2(request)
        return buildList {
            result.objectSummaries.forEach { summary ->
                add(CloudFile(
                    name = summary.key.substringAfterLast('/'),
                    path = summary.key,
                    size = summary.size,
                    lastModified = summary.lastModified.time,
                    isDirectory = false,
                    metadata = getFileMetadata(summary.key)
                ))
            }
            
            result.commonPrefixes.forEach { prefix ->
                add(CloudFile(
                    name = prefix.substringAfterLast('/').removeSuffix("/"),
                    path = prefix,
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
        val metadata = s3Client.getObjectMetadata(bucketName, remotePath)
        return CloudFileMetadata(
            contentType = metadata.contentType,
            eTag = metadata.eTag,
            customMetadata = metadata.userMetadata,
            createdAt = 0, // S3 doesn't provide creation time
            updatedAt = metadata.lastModified.time,
            owner = metadata.owner?.displayName
        )
    }

    override suspend fun getStorageUsage(): Long {
        var totalSize = 0L
        val request = ListObjectsV2Request().withBucketName(bucketName)
        var result: ListObjectsV2Result
        do {
            result = s3Client.listObjectsV2(request)
            result.objectSummaries.forEach { summary ->
                totalSize += summary.size
            }
            request.continuationToken = result.nextContinuationToken
        } while (result.isTruncated)
        return totalSize
    }

    override suspend fun getStorageLimit(): Long {
        // AWS S3 has no built-in storage limit
        // Return a configured limit or max value
        return Long.MAX_VALUE
    }

    override suspend fun createDirectory(remotePath: String) {
        // S3 doesn't have real directories, but we can create a marker object
        val path = if (remotePath.endsWith("/")) remotePath else "$remotePath/"
        val emptyContent = ByteArray(0)
        val metadata = ObjectMetadata().apply {
            contentLength = 0
            contentType = "application/directory"
        }
        s3Client.putObject(bucketName, path, emptyContent.inputStream(), metadata)
    }

    override suspend fun deleteDirectory(remotePath: String) {
        val path = if (remotePath.endsWith("/")) remotePath else "$remotePath/"
        val request = ListObjectsV2Request()
            .withBucketName(bucketName)
            .withPrefix(path)
        
        var result: ListObjectsV2Result
        do {
            result = s3Client.listObjectsV2(request)
            val deleteRequest = DeleteObjectsRequest(bucketName)
                .withKeys(result.objectSummaries.map { DeleteObjectsRequest.KeyVersion(it.key) })
            if (deleteRequest.keys.isNotEmpty()) {
                s3Client.deleteObjects(deleteRequest)
            }
            request.continuationToken = result.nextContinuationToken
        } while (result.isTruncated)
    }

    override suspend fun moveFile(sourcePath: String, destinationPath: String) {
        copyFile(sourcePath, destinationPath)
        deleteFile(sourcePath)
    }

    override suspend fun copyFile(sourcePath: String, destinationPath: String) {
        val request = CopyObjectRequest(bucketName, sourcePath, bucketName, destinationPath)
        s3Client.copyObject(request)
    }

    override suspend fun getFileInputStream(remotePath: String): InputStream {
        val obj = s3Client.getObject(bucketName, remotePath)
        return obj.objectContent
    }

    override suspend fun fileExists(remotePath: String): Boolean {
        return try {
            s3Client.getObjectMetadata(bucketName, remotePath)
            true
        } catch (e: Exception) {
            false
        }
    }
} 