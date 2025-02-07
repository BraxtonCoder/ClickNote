package com.example.clicknote.service

import android.content.Context
import android.util.Log
import com.example.clicknote.analytics.AnalyticsTracker
import com.example.clicknote.data.model.VoskModel
import com.example.clicknote.domain.model.SpeechModel
import com.example.clicknote.domain.model.TranscriptionLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OfflineModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsTracker: AnalyticsTracker
) {
    private var currentModel: SpeechModel? = null
    private var currentLanguage: TranscriptionLanguage? = null
    private val modelLock = Any()

    suspend fun getModel(language: TranscriptionLanguage): Result<SpeechModel> = runCatching {
        synchronized(modelLock) {
            val existingModel = currentModel
            if (existingModel != null && currentLanguage == language) {
                return@runCatching existingModel
            }

            // Clean up old model if exists
            cleanupCurrentModel()

            val startTime = System.currentTimeMillis()
            val modelDir = getOrCreateModelDir(language)

            try {
                val newModel = VoskModel(modelDir.absolutePath)
                if (!newModel.isLoaded()) {
                    throw IOException("Failed to load model")
                }
                
                currentModel = newModel
                currentLanguage = language

                analyticsTracker.trackPerformanceMetric(
                    metricName = "model_load",
                    durationMs = System.currentTimeMillis() - startTime,
                    success = true,
                    additionalData = mapOf(
                        "language" to language.code,
                        "model_size" to modelDir.length()
                    )
                )

                newModel
            } catch (e: IOException) {
                analyticsTracker.trackPerformanceMetric(
                    metricName = "model_load",
                    durationMs = System.currentTimeMillis() - startTime,
                    success = false,
                    additionalData = mapOf(
                        "language" to language.code,
                        "error" to e.message.toString()
                    )
                )
                throw e
            }
        }
    }

    private fun getOrCreateModelDir(language: TranscriptionLanguage): File {
        val modelDir = File(context.getExternalFilesDir(null), "vosk-model-${language.code}")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
            extractModelFiles(language, modelDir)
        }
        return modelDir
    }

    private fun extractModelFiles(language: TranscriptionLanguage, targetDir: File) {
        val assetManager = context.assets
        val modelAssetPath = "vosk/${language.code}"
        
        try {
            assetManager.list(modelAssetPath)?.forEach { fileName ->
                val assetFile = assetManager.open("$modelAssetPath/$fileName")
                val targetFile = File(targetDir, fileName)
                
                FileOutputStream(targetFile).use { output ->
                    assetFile.copyTo(output)
                }
            }
        } catch (e: IOException) {
            Log.e(TAG, "Failed to extract model files", e)
            throw e
        }
    }

    private fun cleanupCurrentModel() {
        synchronized(modelLock) {
            try {
                currentModel?.close()
                currentModel = null
                currentLanguage = null
            } catch (e: Exception) {
                Log.e(TAG, "Error cleaning up model", e)
            }
        }
    }

    fun cleanup() {
        cleanupCurrentModel()
    }

    companion object {
        private const val TAG = "OfflineModelManager"
    }
} 