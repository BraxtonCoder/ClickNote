package com.example.clicknote.service.impl

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.service.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import javax.inject.Provider

@Singleton
class RecordingManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioRecorder: Provider<AudioRecorder>,
    private val transcriptionService: Provider<TranscriptionService>,
    private val audioEnhancer: Provider<AudioEnhancer>,
    private val notificationHandler: Provider<NotificationHandler>,
    private val performanceMonitor: Provider<PerformanceMonitor>,
    private val amplitudeProcessor: Provider<AmplitudeProcessor>,
    private val userPreferences: Provider<UserPreferencesDataStore>
) : RecordingManager {

    private var mediaRecorder: MediaRecorder? = null
    private var recordingJob: Job? = null
    private var amplitudeJob: Job? = null
    private val managerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    
    private val _isRecording = MutableStateFlow(false)
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    private val _transcriptionText = MutableStateFlow("")
    private val _amplitude = MutableStateFlow(0f)
    
    override val isRecording: Flow<Boolean> = _isRecording.asStateFlow()
    override val recordingState: Flow<RecordingState> = _recordingState.asStateFlow()
    override val transcriptionText: Flow<String> = _transcriptionText.asStateFlow()
    override val amplitude: Flow<Float> = _amplitude.asStateFlow()
    override val waveform: Flow<FloatArray> = amplitudeProcessor.get().getWaveformFlow()

    override suspend fun startRecording(outputFile: File) {
        if (_isRecording.value) return
        
        performanceMonitor.get().startOperation("recording_session")
        
        try {
            initializeMediaRecorder(outputFile)
            mediaRecorder?.start()
            
            _isRecording.value = true
            _recordingState.value = RecordingState.Recording
            
            startAmplitudeMonitoring()
            startRealtimeTranscription()
            notificationHandler.get().showRecordingNotification(true)
            
        } catch (e: Exception) {
            handleRecordingError(e)
        }
    }

    override suspend fun stopRecording() {
        if (!_isRecording.value) return
        
        try {
            recordingJob?.cancel()
            amplitudeJob?.cancel()
            
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            
            _recordingState.value = RecordingState.Processing
            _isRecording.value = false
            
            notificationHandler.get().showRecordingNotification(false)
            amplitudeProcessor.get().reset()
            performanceMonitor.get().endOperation("recording_session")
            
        } catch (e: Exception) {
            handleRecordingError(e)
        }
    }

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

    private fun startAmplitudeMonitoring() {
        amplitudeJob = managerScope.launch {
            while (isActive && _isRecording.value) {
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
        managerScope.launch {
            try {
                transcriptionService.get().startRealtimeTranscription { text ->
                    _transcriptionText.value = text
                }
            } catch (e: Exception) {
                // Handle transcription error
            }
        }
    }

    private fun handleRecordingError(e: Exception) {
        _recordingState.value = RecordingState.Error(e)
        cleanup()
    }

    override fun cleanup() {
        recordingJob?.cancel()
        amplitudeJob?.cancel()
        mediaRecorder?.release()
        mediaRecorder = null
        _isRecording.value = false
        _recordingState.value = RecordingState.Idle
        _transcriptionText.value = ""
        amplitudeProcessor.get().reset()
    }
} 