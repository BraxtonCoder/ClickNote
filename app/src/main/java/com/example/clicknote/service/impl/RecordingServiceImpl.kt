package com.example.clicknote.service.impl

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.clicknote.domain.repository.PreferencesRepository
import com.example.clicknote.service.*
import com.example.clicknote.domain.service.RecordingService
import com.example.clicknote.domain.service.RecordingState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import dagger.Lazy

@Singleton
class RecordingServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: Lazy<PreferencesRepository>,
    private val amplitudeProcessor: Lazy<AmplitudeProcessor>,
    private val transcriptionService: Lazy<TranscriptionService>,
    private val notificationHandler: Lazy<NotificationHandler>,
    private val vibrationHandler: Lazy<VibrationHandler>,
    private val performanceMonitor: Lazy<PerformanceMonitor>
) : RecordingService {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var startTime: Long = 0

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: Flow<Boolean> = _isRecording.asStateFlow()

    private val _transcriptionText = MutableStateFlow("")
    override val transcriptionText: Flow<String> = _transcriptionText.asStateFlow()

    override val amplitude: Flow<Float> = amplitudeProcessor.get().getAmplitudeFlow()

    private var amplitudeJob: Job? = null

    private var outputFile: File? = null

    private val _amplitude = MutableStateFlow(0f)
    private val _duration = MutableStateFlow(0L)
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.IDLE)

    override fun startRecording(outputFile: File) {
        if (_recordingState.value == RecordingState.RECORDING) return

        try {
            this.outputFile = outputFile
            mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                MediaRecorder(context)
            } else {
                @Suppress("DEPRECATION")
                MediaRecorder()
            }

            mediaRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            startTime = System.currentTimeMillis()
            _recordingState.value = RecordingState.RECORDING
        } catch (e: Exception) {
            _recordingState.value = RecordingState.ERROR
            release()
            throw e
        }
    }

    override fun stopRecording(): Result<File> = runCatching {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        _recordingState.value = RecordingState.STOPPED
        outputFile ?: throw IllegalStateException("No output file available")
    }

    override fun pauseRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.pause()
            _recordingState.value = RecordingState.PAUSED
        }
    }

    override fun resumeRecording() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mediaRecorder?.resume()
            _recordingState.value = RecordingState.RECORDING
        }
    }

    override fun isRecording(): Boolean = _recordingState.value == RecordingState.RECORDING

    override fun isPaused(): Boolean = _recordingState.value == RecordingState.PAUSED

    override fun getAmplitude(): Flow<Float> {
        return flow {
            while (isRecording()) {
                val amplitude = mediaRecorder?.maxAmplitude?.toFloat() ?: 0f
                emit(amplitude)
                kotlinx.coroutines.delay(100) // Update every 100ms
            }
        }
    }

    override fun getDuration(): Flow<Long> {
        return flow {
            while (isRecording() || isPaused()) {
                val duration = if (startTime > 0) System.currentTimeMillis() - startTime else 0
                emit(duration)
                kotlinx.coroutines.delay(1000) // Update every second
            }
        }
    }

    override fun getRecordingState(): Flow<RecordingState> = _recordingState.asStateFlow()

    override fun getOutputFile(): File? = outputFile

    override fun cancelRecording() {
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: IllegalStateException) {
                // Ignore
            }
            release()
        }
        mediaRecorder = null
        outputFile?.delete()
        outputFile = null
        _recordingState.value = RecordingState.IDLE
    }

    override fun release() {
        mediaRecorder?.release()
        mediaRecorder = null
        outputFile = null
        _recordingState.value = RecordingState.IDLE
    }

    override suspend fun startRecording(): Result<Unit> {
        if (_isRecording.value) {
            return Result.failure(IllegalStateException("Recording is already in progress"))
        }

        return try {
            recordingFile = createAudioFile()
            mediaRecorder = createMediaRecorder(recordingFile!!.absolutePath)
            
            mediaRecorder?.prepare()
            mediaRecorder?.start()
            
            _isRecording.value = true
            _recordingState.value = RecordingState.Recording
            
            startAmplitudeMonitoring()
            vibrationHandler.get().vibrateForRecordingStart()
            
            Result.success(Unit)
        } catch (e: IOException) {
            Result.failure(e)
        }
    }

    override suspend fun stopRecording(): Result<File> {
        if (!_isRecording.value) {
            return Result.failure(IllegalStateException("No recording in progress"))
        }

        return try {
            amplitudeJob?.cancel()
            
            mediaRecorder?.apply {
                stop()
                reset()
                release()
            }
            mediaRecorder = null
            
            _isRecording.value = false
            _recordingState.value = RecordingState.Idle
            _amplitude.value = 0
            
            vibrationHandler.get().vibrateForRecordingStop()
            
            val file = recordingFile!!
            recordingFile = null
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pauseRecording(): Result<Unit> {
        if (!_isRecording.value) {
            return Result.failure(IllegalStateException("No recording in progress"))
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.pause()
                _recordingState.value = RecordingState.Paused
                amplitudeJob?.cancel()
                vibrationHandler.get().vibrateForRecordingPause()
                Result.success(Unit)
            } else {
                Result.failure(UnsupportedOperationException("Pause recording not supported on this device"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resumeRecording(): Result<Unit> {
        if (!_isRecording.value) {
            return Result.failure(IllegalStateException("No recording in progress"))
        }

        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                mediaRecorder?.resume()
                _recordingState.value = RecordingState.Recording
                startAmplitudeMonitoring()
                vibrationHandler.get().vibrateForRecordingResume()
                Result.success(Unit)
            } else {
                Result.failure(UnsupportedOperationException("Resume recording not supported on this device"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun cancelRecording() {
        cleanup()
        _isRecording.value = false
        _transcriptionText.value = ""
        recordingFile?.delete()
        recordingFile = null
    }

    override fun getRecordingDuration(): Long {
        return if (startTime > 0) System.currentTimeMillis() - startTime else 0
    }

    override fun getRecordingFilePath(): String? = recordingFile?.absolutePath

    override suspend fun cleanup() {
        amplitudeJob?.cancel()
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: IllegalStateException) {
                // Ignore
            }
            reset()
            release()
        }
        mediaRecorder = null
        _isRecording.value = false
        notificationHandler.get().cancelRecordingNotification()
    }

    override suspend fun isRecordingAvailable(): Boolean {
        return if (preferencesRepository.get().isPremium.first()) {
            true
        } else {
            preferencesRepository.get().getWeeklyTranscriptionCount().first() < 3
        }
    }

    override fun getTranscriptionMode(): TranscriptionMode {
        return if (preferencesRepository.get().offlineTranscription.value) {
            TranscriptionMode.OFFLINE
        } else {
            TranscriptionMode.ONLINE
        }
    }

    override suspend fun setTranscriptionMode(mode: TranscriptionMode) {
        preferencesRepository.get().setOfflineTranscription(mode == TranscriptionMode.OFFLINE)
    }

    override fun getTranscriptionLanguage(): String {
        return preferencesRepository.get().transcriptionLanguage.value
    }

    override suspend fun setTranscriptionLanguage(language: String) {
        preferencesRepository.get().setTranscriptionLanguage(language)
    }

    private fun createAudioFile(): File {
        val audioDir = File(context.filesDir, "audio").apply { mkdirs() }
        return File(audioDir, "recording_${System.currentTimeMillis()}.m4a")
    }

    private fun createMediaRecorder(outputFile: String): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128000)
            setAudioSamplingRate(44100)
            setOutputFile(outputFile)
            prepare()
        }
    }

    private fun startAmplitudeMonitoring() {
        amplitudeJob?.cancel()
        amplitudeJob = serviceScope.launch {
            while (isActive) {
                try {
                    mediaRecorder?.maxAmplitude?.let { amplitude ->
                        val normalizedAmplitude = amplitude.toFloat() / Short.MAX_VALUE
                        amplitudeProcessor.get().processAmplitude(normalizedAmplitude)
                        _amplitude.value = normalizedAmplitude
                    }
                } catch (e: Exception) {
                    // Ignore amplitude processing errors
                }
                delay(100) // Update amplitude every 100ms
            }
        }
    }

    private fun startRealtimeTranscription() {
        serviceScope.launch {
            try {
                transcriptionService.get().startRealtimeTranscription { text ->
                    _transcriptionText.value = text
                }
            } catch (e: Exception) {
                // Handle transcription error
            }
        }
    }
} 