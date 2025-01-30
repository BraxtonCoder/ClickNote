package com.example.clicknote.service.api.impl

import android.content.Context
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.api.StorageApi
import com.google.firebase.storage.FirebaseStorage
import com.amazonaws.services.s3.AmazonS3Client
import com.azure.storage.blob.BlobServiceClient
import com.google.cloud.storage.Storage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageApiImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore,
    private val firebaseStorage: FirebaseStorage,
    private val s3Client: AmazonS3Client,
    private val blobServiceClient: BlobServiceClient,
    private val googleCloudStorage: Storage
) : StorageApi {

    override suspend fun uploadFile(file: File, path: String): String {
        return when {
            path.startsWith("firebase://") -> uploadToFirebase(file, path.removePrefix("firebase://"))
            path.startsWith("s3://") -> uploadToS3(file, path.removePrefix("s3://"))
            path.startsWith("azure://") -> uploadToAzure(file, path.removePrefix("azure://"))
            path.startsWith("gcs://") -> uploadToGoogleCloud(file, path.removePrefix("gcs://"))
            else -> throw IllegalArgumentException("Invalid storage provider in path: $path")
        }
    }

    override suspend fun downloadFile(path: String): File {
        return when {
            path.startsWith("firebase://") -> downloadFromFirebase(path.removePrefix("firebase://"))
            path.startsWith("s3://") -> downloadFromS3(path.removePrefix("s3://"))
            path.startsWith("azure://") -> downloadFromAzure(path.removePrefix("azure://"))
            path.startsWith("gcs://") -> downloadFromGoogleCloud(path.removePrefix("gcs://"))
            else -> throw IllegalArgumentException("Invalid storage provider in path: $path")
        }
    }

    override suspend fun deleteFile(path: String) {
        when {
            path.startsWith("firebase://") -> deleteFromFirebase(path.removePrefix("firebase://"))
            path.startsWith("s3://") -> deleteFromS3(path.removePrefix("s3://"))
            path.startsWith("azure://") -> deleteFromAzure(path.removePrefix("azure://"))
            path.startsWith("gcs://") -> deleteFromGoogleCloud(path.removePrefix("gcs://"))
            else -> throw IllegalArgumentException("Invalid storage provider in path: $path")
        }
    }

    private suspend fun uploadToFirebase(file: File, path: String): String = withContext(Dispatchers.IO) {
        val ref = firebaseStorage.reference.child(path)
        ref.putFile(android.net.Uri.fromFile(file)).await()
        ref.downloadUrl.await().toString()
    }

    private suspend fun uploadToS3(file: File, path: String): String = withContext(Dispatchers.IO) {
        val parts = path.split("/", limit = 2)
        val bucket = parts[0]
        val key = parts[1]
        s3Client.putObject(bucket, key, file)
        "s3://$path"
    }

    private suspend fun uploadToAzure(file: File, path: String): String = withContext(Dispatchers.IO) {
        val parts = path.split("/", limit = 2)
        val container = parts[0]
        val blobName = parts[1]
        val containerClient = blobServiceClient.getBlobContainerClient(container)
        val blobClient = containerClient.getBlobClient(blobName)
        blobClient.uploadFromFile(file.absolutePath, true)
        "azure://$path"
    }

    private suspend fun uploadToGoogleCloud(file: File, path: String): String = withContext(Dispatchers.IO) {
        val parts = path.split("/", limit = 2)
        val bucket = parts[0]
        val objectName = parts[1]
        val blob = googleCloudStorage.create(
            Storage.BlobInfo.newBuilder(bucket, objectName).build(),
            file.inputStream()
        )
        "gcs://$path"
    }

    private suspend fun downloadFromFirebase(path: String): File = withContext(Dispatchers.IO) {
        val ref = firebaseStorage.reference.child(path)
        val tempFile = createTempFile(path)
        ref.getFile(tempFile).await()
        tempFile
    }

    private suspend fun downloadFromS3(path: String): File = withContext(Dispatchers.IO) {
        val parts = path.split("/", limit = 2)
        val bucket = parts[0]
        val key = parts[1]
        val obj = s3Client.getObject(bucket, key)
        val tempFile = createTempFile(path)
        obj.objectContent.use { input ->
            tempFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        tempFile
    }

    private suspend fun downloadFromAzure(path: String): File = withContext(Dispatchers.IO) {
        val parts = path.split("/", limit = 2)
        val container = parts[0]
        val blobName = parts[1]
        val containerClient = blobServiceClient.getBlobContainerClient(container)
        val blobClient = containerClient.getBlobClient(blobName)
        val tempFile = createTempFile(path)
        blobClient.downloadToFile(tempFile.absolutePath, true)
        tempFile
    }

    private suspend fun downloadFromGoogleCloud(path: String): File = withContext(Dispatchers.IO) {
        val parts = path.split("/", limit = 2)
        val bucket = parts[0]
        val objectName = parts[1]
        val blob = googleCloudStorage.get(bucket, objectName)
        val tempFile = createTempFile(path)
        blob.downloadTo(tempFile.toPath())
        tempFile
    }

    private suspend fun deleteFromFirebase(path: String) = withContext(Dispatchers.IO) {
        firebaseStorage.reference.child(path).delete().await()
    }

    private suspend fun deleteFromS3(path: String) = withContext(Dispatchers.IO) {
        val parts = path.split("/", limit = 2)
        val bucket = parts[0]
        val key = parts[1]
        s3Client.deleteObject(bucket, key)
    }

    private suspend fun deleteFromAzure(path: String) = withContext(Dispatchers.IO) {
        val parts = path.split("/", limit = 2)
        val container = parts[0]
        val blobName = parts[1]
        val containerClient = blobServiceClient.getBlobContainerClient(container)
        val blobClient = containerClient.getBlobClient(blobName)
        blobClient.delete()
    }

    private suspend fun deleteFromGoogleCloud(path: String) = withContext(Dispatchers.IO) {
        val parts = path.split("/", limit = 2)
        val bucket = parts[0]
        val objectName = parts[1]
        googleCloudStorage.delete(bucket, objectName)
    }

    private fun createTempFile(path: String): File {
        val extension = path.substringAfterLast(".", "")
        return File.createTempFile("storage_", ".$extension", context.cacheDir)
    }
} 