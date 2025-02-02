package com.example.clicknote.service.transcription

import android.content.Context
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.TranscriptionLanguage
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import org.whisper.android.WhisperLib
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WhisperTranscriptionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore
) : TranscriptionManager {
    private var whisperLib: WhisperLib? = null
    private var audioBuffer = mutableListOf<Short>()
    private var isInitialized = false

    private suspend fun initializeWhisper() {
        if (isInitialized) return

        withContext(Dispatchers.IO) {
            val modelFile = File(context.getExternalFilesDir(null), "whisper-model.bin")
            if (!modelFile.exists()) {
                // Download model file if not exists
                downloadModel(modelFile)
            }

            whisperLib = WhisperLib(modelFile.absolutePath).apply {
                val language = userPreferences.transcriptionLanguage.first()
                setLanguage(language.code)
                setAutoPunctuation(userPreferences.autoPunctuation.first())
            }
            isInitialized = true
        }
    }

    override suspend fun processAudioData(buffer: ShortArray, size: Int) {
        if (!isInitialized) {
            initializeWhisper()
        }

        // Add new audio data to buffer
        audioBuffer.addAll(buffer.take(size))

        // Process in chunks if buffer gets too large
        if (audioBuffer.size > CHUNK_SIZE) {
            processChunk()
        }
    }

    private suspend fun processChunk() {
        withContext(Dispatchers.Default) {
            val chunk = audioBuffer.take(CHUNK_SIZE).toShortArray()
            whisperLib?.processAudio(chunk)
            audioBuffer = audioBuffer.drop(CHUNK_SIZE).toMutableList()
        }
    }

    override suspend fun finalizeTranscription(): String {
        if (!isInitialized) return ""

        return withContext(Dispatchers.Default) {
            // Process any remaining audio
            if (audioBuffer.isNotEmpty()) {
                whisperLib?.processAudio(audioBuffer.toShortArray())
            }

            // Get final transcription
            val transcription = whisperLib?.getTranscription() ?: ""
            reset()
            transcription
        }
    }

    override suspend fun reset() {
        withContext(Dispatchers.Default) {
            audioBuffer.clear()
            whisperLib?.reset()
        }
    }

    private suspend fun downloadModel(outputFile: File) {
        withContext(Dispatchers.IO) {
            // Implement model downloading logic here
            // This could be from a CDN or local assets
        }
    }

    companion object {
        private const val CHUNK_SIZE = 16000 // Process in 1-second chunks at 16kHz
    }
} 