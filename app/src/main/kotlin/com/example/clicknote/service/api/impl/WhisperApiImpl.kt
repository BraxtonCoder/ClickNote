package com.example.clicknote.service.api.impl

import android.content.Context
import com.example.clicknote.service.WhisperLib
import com.example.clicknote.service.api.WhisperApi
import com.example.clicknote.domain.model.TranscriptionSegment
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhisperApiImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WhisperApi {

    private var whisperModel: Long = 0L
    private var isInitialized = false

    private suspend fun initializeModel() {
        if (!isInitialized) {
            withContext(Dispatchers.IO) {
                val modelFile = File(context.filesDir, "whisper-model.bin")
                if (!modelFile.exists()) {
                    context.assets.open("whisper-tiny-en.tflite").use { input ->
                        modelFile.outputStream().use { output ->
                            input.copyTo(output)
                        }
                    }
                }
                
                whisperModel = WhisperLib.createModel(modelFile.absolutePath)
                if (whisperModel == 0L) {
                    throw IllegalStateException("Failed to create Whisper model")
                }
                isInitialized = true
            }
        }
    }

    override suspend fun transcribe(audioFile: File): String {
        initializeModel()
        return withContext(Dispatchers.IO) {
            WhisperLib.transcribe(whisperModel, audioFile.absolutePath)
        }
    }

    override suspend fun transcribeWithTimestamps(audioFile: File): List<TranscriptionSegment> {
        initializeModel()
        return withContext(Dispatchers.IO) {
            WhisperLib.transcribeWithTimestamps(whisperModel, audioFile.absolutePath)
                .map { it.toTranscriptionSegment() }
        }
    }

    override fun isAvailable(): Boolean = isInitialized

    override fun cleanup() {
        if (isInitialized && whisperModel != 0L) {
            WhisperLib.destroyModel(whisperModel)
            whisperModel = 0L
            isInitialized = false
        }
    }
} 