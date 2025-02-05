package com.example.clicknote.data.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import com.example.clicknote.domain.service.AudioService
import com.example.clicknote.domain.service.RecordingState
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
class AudioServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioService {

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

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.IDLE)
    private val _waveformData = MutableSharedFlow<FloatArray>()
    private val _amplitude = MutableStateFlow(0)

    override suspend fun startRecording(outputFile: File) = withContext(Dispatchers.IO) {
        try {
            this@AudioServiceImpl.outputFile = outputFile
            initializeAudioRecord()
            startTime = System.currentTimeMillis()
            audioRecord?.startRecording()
            _recordingState.value = RecordingState.RECORDING
            startRecordingJob()
            startWaveformJob()
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            throw e
        }
    }

    override suspend fun stopRecording(): Result<File> = withContext(Dispatchers.IO) {
        try {
            recordingJob?.cancel()
            waveformJob?.cancel()
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            _recordingState.value = RecordingState.IDLE
            outputFile?.let { Result.success(it) } ?: Result.failure(IllegalStateException("No output file"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pauseRecording() {
        audioRecord?.stop()
        _recordingState.value = RecordingState.PAUSED
    }

    override suspend fun resumeRecording() {
        audioRecord?.startRecording()
        _recordingState.value = RecordingState.RECORDING
    }

    override fun isRecording(): Boolean {
        return _recordingState.value == RecordingState.RECORDING
    }

    override fun getAmplitude(): Int = _amplitude.value

    override fun getAudioFormat(): AudioFormat {
        return AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AUDIO_FORMAT)
            .setChannelMask(CHANNEL_CONFIG)
            .build()
    }

    override fun getWaveformData(): Flow<FloatArray> = _waveformData

    override suspend fun cleanup() {
        recordingJob?.cancel()
        waveformJob?.cancel()
        audioRecord?.release()
        audioRecord = null
        _recordingState.value = RecordingState.IDLE
    }

    override suspend fun cancelRecording() {
        cleanup()
        outputFile?.delete()
        outputFile = null
    }

    override fun getDuration(): Long {
        return if (startTime > 0) System.currentTimeMillis() - startTime else 0
    }

    override fun getRecordingState(): Flow<RecordingState> = _recordingState

    private fun initializeAudioRecord() {
        audioRecord = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioRecord.Builder()
                .setAudioSource(MediaRecorder.AudioSource.MIC)
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setSampleRate(SAMPLE_RATE)
                        .setEncoding(AUDIO_FORMAT)
                        .setChannelMask(CHANNEL_CONFIG)
                        .build()
                )
                .setBufferSizeInBytes(bufferSize)
                .build()
        } else {
            AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                bufferSize
            )
        }
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