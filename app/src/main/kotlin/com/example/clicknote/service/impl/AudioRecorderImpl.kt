package com.example.clicknote.service.impl

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.AudioManager
import com.example.clicknote.domain.interfaces.AudioRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class AudioRecorderImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioManager: Lazy<AudioManager>
) : AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private val _amplitude = MutableStateFlow(0)
    override val amplitude: Flow<Int> = _amplitude
    override var isRecording: Boolean = false
        private set
    private var isPaused: Boolean = false
    private lateinit var outputFile: File

    override suspend fun startRecording(outputFile: File) {
        if (isRecording) return
        this.outputFile = outputFile
        
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        ).apply {
            if (state != AudioRecord.STATE_INITIALIZED) {
                throw IllegalStateException("Failed to initialize AudioRecord")
            }
        }

        isRecording = true
        isPaused = false
        audioRecord?.startRecording()
        startRecordingThread(bufferSize)
    }

    override suspend fun stopRecording() {
        if (!isRecording) return
        
        isRecording = false
        recordingThread?.join()
        recordingThread = null
        
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        _amplitude.value = 0
    }

    override suspend fun pauseRecording() {
        if (!isRecording || isPaused) return
        isPaused = true
        audioRecord?.stop()
    }

    override suspend fun resumeRecording() {
        if (!isRecording || !isPaused) return
        isPaused = false
        audioRecord?.startRecording()
    }

    override fun getAmplitude(): Int {
        return if (isRecording && !isPaused && audioRecord != null) {
            val buffer = ShortArray(1024)
            val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
            if (read > 0) {
                buffer.take(read).maxOf { kotlin.math.abs(it.toInt()) }
            } else 0
        } else 0
    }

    override fun release() {
        stopRecording()
        audioRecord?.release()
        audioRecord = null
    }

    override fun cleanup() {
        release()
        _amplitude.value = 0
    }

    private fun startRecordingThread(bufferSize: Int) {
        recordingThread = Thread {
            val buffer = ShortArray(bufferSize)
            outputFile.outputStream().buffered().use { outputStream ->
                try {
                    while (isRecording) {
                        if (!isPaused) {
                            val read = audioRecord?.read(buffer, 0, bufferSize) ?: -1
                            if (read > 0) {
                                outputStream.write(buffer.toByteArray(), 0, read * 2)
                                _amplitude.value = buffer.take(read).maxOf { kotlin.math.abs(it.toInt()) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Handle error
                    isRecording = false
                    _amplitude.value = 0
                }
            }
        }.apply { start() }
    }

    private fun ShortArray.toByteArray(): ByteArray {
        val bytes = ByteArray(size * 2)
        for (i in indices) {
            val value = this[i]
            bytes[i * 2] = (value and 0xFF).toByte()
            bytes[i * 2 + 1] = (value.toInt() shr 8).toByte()
        }
        return bytes
    }

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
    }
} 