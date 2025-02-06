package com.example.clicknote.data.service

import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import com.example.clicknote.domain.model.RecordingState
import com.example.clicknote.domain.service.AudioService
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
    private var isRecording = false
    private var isPaused = false

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val bufferSize = AudioRecord.getMinBufferSize(
        SAMPLE_RATE,
        CHANNEL_CONFIG,
        AUDIO_FORMAT
    )

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.IDLE)
    private val _waveformData = MutableSharedFlow<FloatArray>()
    private val _recordingAmplitude = MutableStateFlow(0)
    private val _recordingDuration = MutableStateFlow(0L)
    private val _recordingError = MutableStateFlow<String?>(null)

    override val recordingState: Flow<RecordingState> = _recordingState.asStateFlow()
    override val recordingAmplitude: Flow<Int> = _recordingAmplitude.asStateFlow()
    override val recordingDuration: Flow<Long> = _recordingDuration.asStateFlow()
    override val recordingError: Flow<String?> = _recordingError.asStateFlow()

    override suspend fun startRecording(outputFile: File): Result<Unit> = runCatching {
        if (isRecording) {
            throw IllegalStateException("Already recording")
        }

        _recordingState.value = RecordingState.PREPARING
        
        try {
            this@AudioServiceImpl.outputFile = outputFile
            initializeAudioRecord()
            audioRecord?.startRecording()
            isRecording = true
            isPaused = false
            startTime = System.currentTimeMillis()
            _recordingState.value = RecordingState.RECORDING
            startRecordingJob()
            startWaveformJob()
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            _recordingError.value = e.message
            throw e
        }
    }

    override suspend fun stopRecording(): Result<File> = runCatching {
        if (!isRecording) {
            throw IllegalStateException("Not recording")
        }

        _recordingState.value = RecordingState.STOPPING
        
        try {
            recordingJob?.cancel()
            waveformJob?.cancel()
            audioRecord?.stop()
            audioRecord?.release()
            audioRecord = null
            isRecording = false
            isPaused = false
            _recordingState.value = RecordingState.STOPPED
            
            outputFile?.let { file ->
                if (file.exists() && file.length() > 0) {
                    file
                } else {
                    throw IllegalStateException("Recording file is empty or does not exist")
                }
            } ?: throw IllegalStateException("No output file")
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            _recordingError.value = e.message
            throw e
        }
    }

    override suspend fun pauseRecording(): Result<Unit> = runCatching {
        if (!isRecording || isPaused) {
            throw IllegalStateException("Cannot pause: not recording or already paused")
        }

        audioRecord?.stop()
        isPaused = true
        _recordingState.value = RecordingState.PAUSED
    }

    override suspend fun resumeRecording(): Result<Unit> = runCatching {
        if (!isRecording || !isPaused) {
            throw IllegalStateException("Cannot resume: not recording or not paused")
        }

        audioRecord?.startRecording()
        isPaused = false
        _recordingState.value = RecordingState.RECORDING
    }

    override suspend fun cancelRecording() {
        recordingJob?.cancel()
        waveformJob?.cancel()
        audioRecord?.release()
        audioRecord = null
        isRecording = false
        isPaused = false
        _recordingState.value = RecordingState.IDLE
        outputFile?.delete()
        outputFile = null
    }

    override suspend fun isRecording(): Boolean = isRecording

    override suspend fun cleanup() {
        cancelRecording()
    }

    override fun getAudioFormat(): AudioFormat {
        return AudioFormat.Builder()
            .setSampleRate(SAMPLE_RATE)
            .setEncoding(AUDIO_FORMAT)
            .setChannelMask(CHANNEL_CONFIG)
            .build()
    }

    override fun getWaveformData(): Flow<FloatArray> = _waveformData

    override fun getDuration(): Long {
        return if (isRecording && !isPaused) {
            System.currentTimeMillis() - startTime
        } else {
            0L
        }
    }

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
        _recordingAmplitude.value = if (readSize > 0) sum / readSize else 0
    }

    private fun abs(value: Int): Int = if (value < 0) -value else value

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val WAVEFORM_UPDATE_INTERVAL = 50L // 50ms update interval
    }
} 