package com.example.clicknote.service.cloud

import android.content.Context
import android.net.Uri
import com.example.clicknote.analytics.AnalyticsManager
import com.example.clicknote.domain.model.CallRecording
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudStorageService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore,
    private val analyticsManager: AnalyticsManager,
    private val firebaseAuth: FirebaseAuth,
    private val firebaseStorage: FirebaseStorage
) {
    private val storageRef = firebaseStorage.reference

    suspend fun uploadCallRecording(recording: CallRecording): Result<String> = withContext(Dispatchers.IO) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(
                IllegalStateException("User not authenticated")
            )

            // Check if cloud sync is enabled
            if (!userPreferences.isCloudSyncEnabled().first()) {
                return@withContext Result.failure(IllegalStateException("Cloud sync is disabled"))
            }

            val audioFile = File(recording.audioFilePath)
            if (!audioFile.exists()) {
                return@withContext Result.failure(IllegalStateException("Audio file not found"))
            }

            // Create reference to the audio file in Firebase Storage
            val audioRef = storageRef
                .child("users")
                .child(userId)
                .child("recordings")
                .child("${recording.timestamp}_${audioFile.name}")

            // Upload audio file
            val uploadTask = audioRef.putFile(Uri.fromFile(audioFile))
            val downloadUrl = uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                audioRef.downloadUrl
            }.await()

            // Track successful upload
            analyticsManager.trackCloudUploadCompleted(
                fileType = "audio",
                fileSize = audioFile.length(),
                duration = recording.duration
            )

            Result.success(downloadUrl.toString())
        } catch (e: Exception) {
            // Track upload error
            analyticsManager.trackCloudUploadError(
                error = e.message ?: "Unknown error",
                fileType = "audio"
            )
            Result.failure(e)
        }
    }

    suspend fun downloadCallRecording(recording: CallRecording): Result<File> = withContext(Dispatchers.IO) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(
                IllegalStateException("User not authenticated")
            )

            // Check if cloud sync is enabled
            if (!userPreferences.isCloudSyncEnabled().first()) {
                return@withContext Result.failure(IllegalStateException("Cloud sync is disabled"))
            }

            val audioRef = storageRef
                .child("users")
                .child(userId)
                .child("recordings")
                .child("${recording.timestamp}_${File(recording.audioFilePath).name}")

            val localFile = File(context.cacheDir, "temp_${System.currentTimeMillis()}.m4a")
            audioRef.getFile(localFile).await()

            // Track successful download
            analyticsManager.trackCloudDownloadCompleted(
                fileType = "audio",
                fileSize = localFile.length(),
                duration = recording.duration
            )

            Result.success(localFile)
        } catch (e: Exception) {
            // Track download error
            analyticsManager.trackCloudDownloadError(
                error = e.message ?: "Unknown error",
                fileType = "audio"
            )
            Result.failure(e)
        }
    }

    suspend fun deleteCallRecording(recording: CallRecording): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(
                IllegalStateException("User not authenticated")
            )

            val audioRef = storageRef
                .child("users")
                .child(userId)
                .child("recordings")
                .child("${recording.timestamp}_${File(recording.audioFilePath).name}")

            audioRef.delete().await()

            // Track successful deletion
            analyticsManager.trackCloudDeletionCompleted(fileType = "audio")

            Result.success(Unit)
        } catch (e: Exception) {
            // Track deletion error
            analyticsManager.trackCloudDeletionError(
                error = e.message ?: "Unknown error",
                fileType = "audio"
            )
            Result.failure(e)
        }
    }

    suspend fun syncCallRecordings(recordings: List<CallRecording>): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val userId = firebaseAuth.currentUser?.uid ?: return@withContext Result.failure(
                IllegalStateException("User not authenticated")
            )

            // Check if cloud sync is enabled
            if (!userPreferences.isCloudSyncEnabled().first()) {
                return@withContext Result.failure(IllegalStateException("Cloud sync is disabled"))
            }

            // Get list of all recordings in cloud
            val cloudRecordings = storageRef
                .child("users")
                .child(userId)
                .child("recordings")
                .listAll()
                .await()

            // Create map of cloud recordings by timestamp
            val cloudRecordingMap = cloudRecordings.items.associate { ref ->
                val timestamp = ref.name.substringBefore("_").toLongOrNull() ?: 0L
                timestamp to ref
            }

            // Upload missing recordings
            recordings.forEach { recording ->
                if (!cloudRecordingMap.containsKey(recording.timestamp)) {
                    uploadCallRecording(recording)
                }
            }

            // Delete cloud recordings that don't exist locally
            cloudRecordingMap.forEach { (timestamp, ref) ->
                if (recordings.none { it.timestamp == timestamp }) {
                    ref.delete().await()
                }
            }

            // Update last sync time
            userPreferences.setLastSyncTime(System.currentTimeMillis())

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 