package com.example.clicknote.service

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.vosk.Model
import org.vosk.Recognizer
import org.vosk.android.SpeechService
import org.vosk.android.StorageService
import java.io.File
import java.io.FileInputStream
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : TranscriptionService {

    private var model: Model? = null
    private var recognizer: Recognizer? = null
    private var speechService: SpeechService? = null

    private val _isTranscribing = MutableStateFlow(false)
    override val isTranscribing: StateFlow<Boolean> = _isTranscribing

    private val _progress = MutableStateFlow(0f)
    override val progress: StateFlow<Float> = _progress

    init {
        initializeModel()
    }

    private fun initializeModel() {
        val modelDir = File(context.getExternalFilesDir(null), "vosk-model")
        if (!modelDir.exists()) {
            modelDir.mkdirs()
            // Copy model files from assets to external storage
            StorageService.unpack(context, "vosk-model-small-en-us", modelDir.path)
        }
        model = Model(modelDir.path)
    }

    override suspend fun transcribe(audioPath: String): TranscriptionResult = withContext(Dispatchers.IO) {
        try {
            _isTranscribing.value = true
            _progress.value = 0f

            val audioFile = File(audioPath)
            val recognizer = Recognizer(model, 16000.0f)
            
            val buffer = ByteArray(4096)
            val inputStream = FileInputStream(audioFile)
            
            var text = ""
            var bytesRead: Int
            var totalBytes = 0L
            val fileSize = audioFile.length()

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    text += recognizer.result + " "
                }
                totalBytes += bytesRead
                _progress.value = totalBytes.toFloat() / fileSize
            }

            text += recognizer.finalResult

            recognizer.close()
            
            TranscriptionResult(
                text = text.trim(),
                segments = emptyList(), // TODO: Implement segmentation
                speakers = emptyList() // TODO: Implement speaker detection
            )
        } finally {
            _isTranscribing.value = false
            _progress.value = 1f
        }
    }

    override suspend fun transcribeInRealTime(audioPath: String): Flow<TranscriptionResult> = flow {
        try {
            _isTranscribing.value = true
            _progress.value = 0f

            val audioFile = File(audioPath)
            val recognizer = Recognizer(model, 16000.0f)
            
            val buffer = ByteArray(4096)
            val inputStream = FileInputStream(audioFile)
            
            var bytesRead: Int
            var totalBytes = 0L
            val fileSize = audioFile.length()

            while (inputStream.read(buffer).also { bytesRead = it } != -1) {
                if (recognizer.acceptWaveForm(buffer, bytesRead)) {
                    emit(TranscriptionResult(
                        text = recognizer.result,
                        segments = emptyList(),
                        speakers = emptyList()
                    ))
                }
                totalBytes += bytesRead
                _progress.value = totalBytes.toFloat() / fileSize
            }

            emit(TranscriptionResult(
                text = recognizer.finalResult,
                segments = emptyList(),
                speakers = emptyList()
            ))

            recognizer.close()
        } finally {
            _isTranscribing.value = false
            _progress.value = 1f
        }
    }

    override suspend fun cancelTranscription() {
        speechService?.stop()
        speechService = null
        _isTranscribing.value = false
        _progress.value = 0f
    }
} 