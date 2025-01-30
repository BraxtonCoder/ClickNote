package com.example.clicknote.worker

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import kotlin.math.roundToInt

class ModelDownloadWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val modelName = inputData.getString("model_name")
                ?: return@withContext Result.failure(workDataOf("error" to "Model name not provided"))
            
            val modelUrl = inputData.getString("model_url")
                ?: return@withContext Result.failure(workDataOf("error" to "Model URL not provided"))
            
            val modelSize = inputData.getLong("model_size", 0)
            if (modelSize <= 0) {
                return@withContext Result.failure(workDataOf("error" to "Invalid model size"))
            }

            val modelFile = File(context.getExternalFilesDir("models"), "$modelName.tflite")
            modelFile.parentFile?.mkdirs()

            val url = URL(modelUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 15000
            connection.readTimeout = 15000

            var downloadedBytes = 0L
            var lastProgressUpdate = 0

            connection.inputStream.use { input ->
                FileOutputStream(modelFile).use { output ->
                    val buffer = ByteArray(8192)
                    var bytes: Int

                    while (input.read(buffer).also { bytes = it } >= 0) {
                        output.write(buffer, 0, bytes)
                        downloadedBytes += bytes

                        // Update progress every 1%
                        val currentProgress = ((downloadedBytes.toFloat() / modelSize) * 100).roundToInt()
                        if (currentProgress > lastProgressUpdate) {
                            setProgress(workDataOf("progress" to currentProgress.toFloat() / 100))
                            lastProgressUpdate = currentProgress
                        }
                    }
                }
            }

            // Verify downloaded file size
            if (modelFile.length() != modelSize) {
                modelFile.delete()
                return@withContext Result.failure(
                    workDataOf("error" to "Downloaded file size mismatch")
                )
            }

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Model download failed", e)
            Result.failure(workDataOf("error" to e.message))
        }
    }

    companion object {
        private const val TAG = "ModelDownloadWorker"
    }
} 