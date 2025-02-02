package com.example.clicknote.service

import android.content.Context
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipInputStream
import javax.inject.Inject
import javax.inject.Singleton
import java.io.IOException
import org.vosk.Model

sealed class ModelError : Exception() {
    object NetworkError : ModelError()
    object StorageError : ModelError()
    object ModelLoadError : ModelError()
}

@Singleton
class OfflineModelManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "OfflineModelManager"
        private const val VOSK_MODEL_URL = "https://alphacephei.com/vosk/models/vosk-model-small-en-us-0.15.zip"
        private const val VOSK_MODEL_DIR = "vosk-model-small-en-us"
    }

    private var model: Model? = null

    suspend fun ensureModelExists(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            if (!hasModel()) {
                downloadVoskModel().collect()
            }
            Result.success(Unit)
        } catch (e: ModelError) {
            Result.failure(e)
        }
    }

    private fun downloadVoskModel(): Flow<Float> = flow {
        try {
            val modelDir = File(context.filesDir, VOSK_MODEL_DIR)
            if (!modelDir.exists()) {
                modelDir.mkdirs()
            }

            val url = URL(VOSK_MODEL_URL)
            val connection = url.openConnection()
            val contentLength = connection.contentLength.toFloat()
            var downloadedBytes = 0f

            connection.getInputStream().use { input ->
                val zipStream = ZipInputStream(input)
                var entry = zipStream.nextEntry
                while (entry != null) {
                    val file = File(modelDir, entry.name)
                    if (entry.isDirectory) {
                        file.mkdirs()
                    } else {
                        FileOutputStream(file).use { output ->
                            val buffer = ByteArray(8192)
                            var bytes = zipStream.read(buffer)
                            while (bytes >= 0) {
                                output.write(buffer, 0, bytes)
                                downloadedBytes += bytes
                                emit(downloadedBytes / contentLength)
                                bytes = zipStream.read(buffer)
                            }
                        }
                    }
                    zipStream.closeEntry()
                    entry = zipStream.nextEntry
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to download Vosk model", e)
            throw ModelError.NetworkError
        }
    }.flowOn(Dispatchers.IO)

    fun getModel(): Model? {
        if (model == null) {
            val modelDir = File(context.filesDir, VOSK_MODEL_DIR)
            if (modelDir.exists()) {
                model = Model(modelDir.absolutePath)
            }
        }
        return model
    }

    fun hasModel(): Boolean {
        val modelDir = File(context.filesDir, VOSK_MODEL_DIR)
        return modelDir.exists() && modelDir.list()?.isNotEmpty() == true
    }

    fun releaseModel() {
        model?.close()
        model = null
    }
} 