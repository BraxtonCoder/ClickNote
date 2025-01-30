package com.example.clicknote.service.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.clicknote.service.AudioEnhancer
import com.example.clicknote.service.AudioConverter
import com.example.clicknote.service.AudioPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton

@Singleton
class AudioServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioEnhancer: Provider<AudioEnhancer>,
    private val audioConverter: Provider<AudioConverter>,
    private val audioPlayer: Provider<AudioPlayer>
) : AudioService {
    private var audioRecord: AudioRecord? = null
    private var recordingThread: Thread? = null
    private val recordingState = MutableStateFlow(RecordingState.IDLE)
    private var isRecording = false
    private var isPaused = false
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
        recordingState.value = RecordingState.RECORDING

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
        
        recordingState.value = RecordingState.STOPPED
    }

    override suspend fun pauseRecording() {
        if (!isRecording || isPaused) return
        isPaused = true
        recordingState.value = RecordingState.PAUSED
    }

    override suspend fun resumeRecording() {
        if (!isRecording || !isPaused) return
        isPaused = false
        recordingState.value = RecordingState.RECORDING
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

    override fun getRecordingState(): Flow<RecordingState> = recordingState

    override suspend fun enhanceAudio(inputFile: File, outputFile: File) {
        withContext(Dispatchers.IO) {
            audioEnhancer.get().enhance(inputFile, outputFile)
        }
    }

    override suspend fun convertAudio(inputFile: File, outputFile: File, format: AudioFormat) {
        withContext(Dispatchers.IO) {
            audioConverter.get().convert(inputFile, outputFile, format)
        }
    }

    override suspend fun playAudio(file: File) {
        audioPlayer.get().play(file)
    }

    override suspend fun stopPlayback() {
        audioPlayer.get().stop()
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
                            }
                        }
                    }
                } catch (e: Exception) {
                    recordingState.value = RecordingState.ERROR
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