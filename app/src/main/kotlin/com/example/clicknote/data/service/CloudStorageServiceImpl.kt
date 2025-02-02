package com.example.clicknote.data.service

import android.content.Context
import android.net.Uri
import com.example.clicknote.domain.service.CloudStorageService
import com.example.clicknote.domain.service.FileMetadata
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudStorageServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val storage: FirebaseStorage
) : CloudStorageService {

    private val _uploadProgress = MutableStateFlow(0f)
    private val _downloadProgress = MutableStateFlow(0f)
    private var currentTask: Any? = null

    override suspend fun uploadFile(userId: String, path: String, file: File): Result<String> = runCatching {
        val ref = storage.reference.child("users/$userId/$path")
        val metadata = StorageMetadata.Builder()
            .setContentType(context.contentResolver.getType(Uri.fromFile(file)))
            .build()

        currentTask = ref.putFile(Uri.fromFile(file), metadata)
            .addOnProgressListener { taskSnapshot ->
                val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount
                _uploadProgress.value = progress
            }
            .await()

        ref.downloadUrl.await().toString()
    }

    override suspend fun uploadFile(userId: String, path: String, uri: Uri): Result<String> = runCatching {
        val ref = storage.reference.child("users/$userId/$path")
        val metadata = StorageMetadata.Builder()
            .setContentType(context.contentResolver.getType(uri))
            .build()

        currentTask = ref.putFile(uri, metadata)
            .addOnProgressListener { taskSnapshot ->
                val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount
                _uploadProgress.value = progress
            }
            .await()

        ref.downloadUrl.await().toString()
    }

    override suspend fun downloadFile(userId: String, path: String): Result<File> = runCatching {
        val ref = storage.reference.child("users/$userId/$path")
        val localFile = File(context.cacheDir, path.substringAfterLast('/'))

        currentTask = ref.getFile(localFile)
            .addOnProgressListener { taskSnapshot ->
                val progress = taskSnapshot.bytesTransferred.toFloat() / taskSnapshot.totalByteCount
                _downloadProgress.value = progress
            }
            .await()

        localFile
    }

    override suspend fun deleteFile(userId: String, path: String): Result<Unit> = runCatching {
        storage.reference.child("users/$userId/$path").delete().await()
    }

    override suspend fun getFileMetadata(userId: String, path: String): Result<FileMetadata> = runCatching {
        val ref = storage.reference.child("users/$userId/$path")
        val metadata = ref.metadata.await()
        val downloadUrl = ref.downloadUrl.await().toString()

        FileMetadata(
            name = ref.name,
            path = ref.path,
            size = metadata.sizeBytes,
            mimeType = metadata.contentType ?: "",
            createdAt = metadata.creationTimeMillis,
            updatedAt = metadata.updatedTimeMillis,
            downloadUrl = downloadUrl
        )
    }

    override suspend fun listFiles(userId: String, path: String): Result<List<FileMetadata>> = runCatching {
        val ref = storage.reference.child("users/$userId/$path")
        val result = ref.listAll().await()

        result.items.map { item ->
            val metadata = item.metadata.await()
            val downloadUrl = item.downloadUrl.await().toString()

            FileMetadata(
                name = item.name,
                path = item.path,
                size = metadata.sizeBytes,
                mimeType = metadata.contentType ?: "",
                createdAt = metadata.creationTimeMillis,
                updatedAt = metadata.updatedTimeMillis,
                downloadUrl = downloadUrl
            )
        }
    }

    override suspend fun getStorageUsage(userId: String): Result<Long> = runCatching {
        val ref = storage.reference.child("users/$userId")
        val result = ref.listAll().await()
        
        var totalSize = 0L
        result.items.forEach { item ->
            totalSize += item.metadata.await().sizeBytes
        }
        
        result.prefixes.forEach { prefix ->
            val prefixResult = prefix.listAll().await()
            prefixResult.items.forEach { item ->
                totalSize += item.metadata.await().sizeBytes
            }
        }
        
        totalSize
    }

    override fun getUploadProgress(): Flow<Float> = _uploadProgress

    override fun getDownloadProgress(): Flow<Float> = _downloadProgress

    override fun cancelOperation() {
        when (currentTask) {
            is com.google.firebase.storage.UploadTask -> (currentTask as com.google.firebase.storage.UploadTask).cancel()
            is com.google.firebase.storage.FileDownloadTask -> (currentTask as com.google.firebase.storage.FileDownloadTask).cancel()
        }
        currentTask = null
        _uploadProgress.value = 0f
        _downloadProgress.value = 0f
    }

    override suspend fun isOperationInProgress(): Boolean {
        return currentTask != null
    }
} 