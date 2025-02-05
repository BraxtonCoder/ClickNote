package com.example.clicknote.data.audio

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import com.example.clicknote.domain.audio.AudioRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AudioRecorderImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var waveformJob: Job? = null
    private var outputFile: File? = null
    private var startTime: Long = 0

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        CHANNEL_CONFIG,
        AUDIO_FORMAT
    )

    private val _waveformData = MutableSharedFlow<FloatArray>()
    private val _amplitude = MutableStateFlow(0)

    override fun start(outputFile: File) {
        try {
            this.outputFile = outputFile
            initializeAudioRecord()
            startTime = System.currentTimeMillis()
            audioRecord?.startRecording()
            startRecordingJob()
            startWaveformJob()
        } catch (e: Exception) {
            cleanup()
            throw e
        }
    }

    override fun stop() {
        recordingJob?.cancel()
        waveformJob?.cancel()
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        outputFile = null
    }

    override fun pause() {
        audioRecord?.stop()
    }

    override fun resume() {
        audioRecord?.startRecording()
    }

    override fun isRecording(): Boolean {
        return audioRecord?.recordingState == AudioRecord.RECORDSTATE_RECORDING
    }

    override fun getAmplitude(): Int = _amplitude.value

    override fun getWaveformData(): Flow<FloatArray> = _waveformData

    override fun getDuration(): Long {
        return if (startTime > 0) System.currentTimeMillis() - startTime else 0
    }

    override fun cleanup() {
        stop()
    }

    private fun initializeAudioRecord() {
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            CHANNEL_CONFIG,
            AUDIO_FORMAT,
            bufferSize
        )
    }

    private fun startRecordingJob() {
        recordingJob = scope.launch {
            val buffer = ByteArray(bufferSize)
            outputFile?.let { file ->
                FileOutputStream(file).use { output ->
                    while (isActive) {
                        val readSize = audioRecord?.read(buffer, 0, bufferSize) ?: -1
                        if (readSize > 0) {
                            output.write(buffer, 0, readSize)
                            calculateAmplitude(buffer, readSize)
                        }
                    }
                }
            }
        }
    }

    private fun startWaveformJob() {
        waveformJob = scope.launch {
            val buffer = ShortArray(bufferSize / 2)
            val floatBuffer = FloatArray(buffer.size)
            
            while (isActive) {
                val readSize = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                if (readSize > 0) {
                    for (i in 0 until readSize) {
                        floatBuffer[i] = buffer[i].toFloat() / Short.MAX_VALUE
                    }
                    _waveformData.emit(floatBuffer.copyOf(readSize))
                }
                delay(WAVEFORM_UPDATE_INTERVAL)
            }
        }
    }

    private fun calculateAmplitude(buffer: ByteArray, readSize: Int) {
        val shorts = ShortArray(readSize / 2)
        ByteBuffer.wrap(buffer, 0, readSize)
            .order(ByteOrder.LITTLE_ENDIAN)
            .asShortBuffer()
            .get(shorts)

        var sum = 0
        for (value in shorts) {
            sum += abs(value.toInt())
        }
        _amplitude.value = if (readSize > 0) sum / readSize else 0
    }

    private fun abs(value: Int): Int = if (value < 0) -value else value

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val WAVEFORM_UPDATE_INTERVAL = 50L // 50ms update interval
    }
} 