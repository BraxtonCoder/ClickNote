package com.example.clicknote.service.impl

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.interfaces.TranscriptionEventHandler
import com.example.clicknote.domain.interfaces.TranscriptionStateManager
import com.example.clicknote.domain.interfaces.RecordingManager
import com.example.clicknote.domain.interfaces.RecordingState
import com.example.clicknote.service.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

class RecordingManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioRecorder: AudioRecorder,
    private val eventHandler: TranscriptionEventHandler,
    private val stateManager: TranscriptionStateManager,
    private val audioEnhancer: AudioEnhancer,
    private val amplitudeProcessor: AmplitudeProcessor,
    private val userPreferences: UserPreferencesDataStore
) : RecordingManager {

    private var mediaRecorder: MediaRecorder? = null
    private var recordingJob: Job? = null
    private var amplitudeJob: Job? = null
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _isRecording = MutableStateFlow(false)
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    private val _recordingDuration = MutableStateFlow(0L)
    private val _amplitude = MutableStateFlow(0f)
    private val _error = MutableSharedFlow<Throwable>()
    
    override val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()
    override val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    override val recordingDuration: StateFlow<Long> = _recordingDuration.asStateFlow()
    override val amplitude: StateFlow<Float> = _amplitude.asStateFlow()
    override val waveform: StateFlow<FloatArray> = amplitudeProcessor.getWaveformFlow()
    override val transcriptionResult: SharedFlow<String> = eventHandler.getTranscriptionStateFlow()
        .map { state -> 
            when (state) {
                is TranscriptionState.Success -> state.text
                else -> ""
            }
        }
        .shareIn(managerScope, SharingStarted.Eagerly, 1)
    override val error: SharedFlow<Throwable> = _error.asSharedFlow()

    override suspend fun startRecording() {
        if (_recordingState.value is RecordingState.Recording) return
        
        try {
            audioRecorder.startRecording()
            audioEnhancer.startProcessing()
            eventHandler.onTranscriptionStarted()
            _isRecording.value = true
            _recordingState.value = RecordingState.Recording
            startAmplitudeMonitoring()
            startDurationTracking()
        } catch (e: Exception) {
            _error.emit(e)
            _recordingState.value = RecordingState.Error(e.message ?: "Unknown error occurred")
            throw e
        }
    }

    override suspend fun stopRecording() {
        try {
            audioRecorder.stopRecording()
            audioEnhancer.stopProcessing()
            amplitudeProcessor.reset()
            _isRecording.value = false
            _recordingState.value = RecordingState.Idle
            recordingJob?.cancel()
            amplitudeJob?.cancel()
        } catch (e: Exception) {
            _error.emit(e)
            _recordingState.value = RecordingState.Error(e.message ?: "Unknown error occurred")
            throw e
        }
    }

    override suspend fun pauseRecording() {
        try {
            audioRecorder.pauseRecording()
            audioEnhancer.pauseProcessing()
            _isRecording.value = false
            _recordingState.value = RecordingState.Paused
            recordingJob?.cancel()
            amplitudeJob?.cancel()
        } catch (e: Exception) {
            _error.emit(e)
            _recordingState.value = RecordingState.Error(e.message ?: "Unknown error occurred")
            throw e
        }
    }

    override suspend fun resumeRecording() {
        try {
            audioRecorder.resumeRecording()
            audioEnhancer.resumeProcessing()
            _isRecording.value = true
            _recordingState.value = RecordingState.Recording
            startAmplitudeMonitoring()
            startDurationTracking()
        } catch (e: Exception) {
            _error.emit(e)
            _recordingState.value = RecordingState.Error(e.message ?: "Unknown error occurred")
            throw e
        }
    }

    override suspend fun cleanup() {
        try {
            stopRecording()
            audioRecorder.release()
            mediaRecorder?.release()
            mediaRecorder = null
            managerScope.cancel()
        } catch (e: Exception) {
            _error.emit(e)
            _recordingState.value = RecordingState.Error(e.message ?: "Unknown error occurred")
            throw e
        }
    }

    private fun startAmplitudeMonitoring() {
        amplitudeJob?.cancel()
        amplitudeJob = managerScope.launch {
            while (isActive) {
                val maxAmplitude = audioRecorder.getMaxAmplitude()
                _amplitude.value = maxAmplitude.toFloat()
                delay(50) // Update amplitude every 50ms
            }
        }
    }

    private fun startDurationTracking() {
        recordingJob?.cancel()
        recordingJob = managerScope.launch {
            var duration = _recordingDuration.value
            while (isActive) {
                delay(1000) // Update duration every second
                duration += 1000
                _recordingDuration.value = duration
            }
        }
    }

    override fun getAmplitude(): Float = _amplitude.value

    private fun initializeMediaRecorder(outputFile: File) {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
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
            setOutputFile(outputFile.absolutePath)
            prepare()
        }
    }

    private fun handleRecordingError(e: Exception) {
        managerScope.launch {
            _error.emit(e)
            eventHandler.onTranscriptionError(e)
        }
        cleanup()
    }

    private fun createOutputFile(): File {
        val dir = File(context.filesDir, "recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "recording_${System.currentTimeMillis()}.m4a")
    }

    private fun getCurrentRecordingFile(): File? {
        val dir = File(context.filesDir, "recordings")
        return dir.listFiles()?.maxByOrNull { it.lastModified() }
    }
} 