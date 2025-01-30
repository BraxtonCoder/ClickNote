package com.example.clicknote.ui.recording

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.service.AudioRecorder
import com.example.clicknote.service.RecordingAnalyticsService
import com.example.clicknote.service.PremiumFeature
import com.example.clicknote.service.PremiumFeatureManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject
import java.util.UUID
import kotlin.math.max

enum class RecordingState {
    IDLE, RECORDING, PAUSED
}

data class RecordingUiState(
    val recordingState: RecordingState = RecordingState.IDLE,
    val recordingDuration: Long = 0L,
    val currentAmplitude: Int = 0,
    val amplitudeHistory: List<Int> = emptyList(),
    val transcription: String = "",
    val error: String? = null,
    val isLoading: Boolean = false,
    val hasMicrophonePermission: Boolean = false,
    val showUpgradePrompt: Boolean = false,
    val remainingTranscriptions: Int = 0
)

@HiltViewModel
class RecordingViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioRecorder: AudioRecorder,
    private val transcriptionService: TranscriptionService,
    private val noteRepository: NoteRepository,
    private val analyticsService: RecordingAnalyticsService,
    private val premiumFeatureManager: PremiumFeatureManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordingUiState())
    val uiState: StateFlow<RecordingUiState> = _uiState.asStateFlow()

    private var maxAmplitude = 1 // Avoid division by zero
    private val amplitudeHistorySize = 100 // Keep last 100 amplitude values
    private var recorder: MediaRecorder? = null
    private var audioFile: File? = null
    private var recordingJob: Job? = null
    private var transcriptionJob: Job? = null
    private var startTime: Long = 0L

    init {
        observeRecordingState()
    }

    private fun observeRecordingState() {
        viewModelScope.launch {
            audioRecorder.amplitude
                .collect { amplitude ->
                    updateAmplitude(amplitude)
                }
        }

        viewModelScope.launch {
            audioRecorder.duration
                .collect { duration ->
                    _uiState.update { it.copy(recordingDuration = duration) }
                }
        }
    }

    private fun updateAmplitude(newAmplitude: Float) {
        val normalizedAmplitude = (newAmplitude * 100).toInt().coerceIn(0, 100)
        
        val currentHistory = _uiState.value.amplitudeHistory
        val updatedHistory = if (currentHistory.size >= amplitudeHistorySize) {
            currentHistory.drop(1) + normalizedAmplitude
        } else {
            currentHistory + normalizedAmplitude
        }

        _uiState.update { 
            it.copy(
                currentAmplitude = normalizedAmplitude,
                amplitudeHistory = updatedHistory
            )
        }
    }

    fun checkMicrophonePermission() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        _uiState.update { it.copy(hasMicrophonePermission = hasPermission) }
    }

    fun requestMicrophonePermission() {
        // This will be handled by the Activity/Fragment
        // The result will trigger checkMicrophonePermission()
    }

    fun startRecording() {
        viewModelScope.launch {
            if (premiumFeatureManager.canUseFeature(PremiumFeature.TRANSCRIPTION)) {
                _uiState.update { it.copy(isRecording = true) }
                audioRecorder.startRecording()
                startTime = System.currentTimeMillis()
                startTranscription()
                premiumFeatureManager.incrementTranscriptionCount()
            } else {
                val remainingTranscriptions = premiumFeatureManager.getRemainingTranscriptions()
                _uiState.update { 
                    it.copy(
                        showUpgradePrompt = true,
                        remainingTranscriptions = remainingTranscriptions
                    )
                }
                analyticsService.trackUpgradePromptShown(
                    feature = PremiumFeature.TRANSCRIPTION,
                    remainingCount = remainingTranscriptions,
                    source = "recording_screen"
                )
                // Trigger vibration to indicate limit reached
                vibrationManager.vibrateError()
            }
        }
    }

    fun pauseRecording() {
        viewModelScope.launch {
            try {
                audioRecorder.pauseRecording()
                _uiState.update { it.copy(
                    recordingState = RecordingState.PAUSED,
                    isLoading = false
                )}
                analyticsService.logRecordingPaused(uiState.value.recordingDuration)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to pause recording: ${e.message}"
                )}
                analyticsService.logRecordingError(e)
            }
        }
    }

    fun resumeRecording() {
        viewModelScope.launch {
            try {
                audioRecorder.resumeRecording()
                _uiState.update { it.copy(
                    recordingState = RecordingState.RECORDING,
                    error = null,
                    isLoading = true
                )}
                startTranscription()
                analyticsService.logRecordingResumed()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to resume recording: ${e.message}"
                )}
                analyticsService.logRecordingError(e)
            }
        }
    }

    fun stopRecording() {
        viewModelScope.launch {
            try {
                audioRecorder.stopRecording()
                transcriptionJob?.cancel()
                _uiState.update { it.copy(
                    recordingState = RecordingState.IDLE,
                    isLoading = false
                )}
                analyticsService.logRecordingStopped(uiState.value.recordingDuration)
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to stop recording: ${e.message}"
                )}
                analyticsService.logRecordingError(e)
            }
        }
    }

    fun saveRecording() {
        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true) }
                
                val audioFile = audioRecorder.getRecordingFile()
                val transcription = uiState.value.transcription
                
                val note = Note(
                    id = UUID.randomUUID().toString(),
                    content = transcription,
                    audioPath = audioFile?.absolutePath,
                    hasAudio = audioFile != null,
                    duration = uiState.value.recordingDuration,
                    createdAt = LocalDateTime.now(),
                    updatedAt = LocalDateTime.now()
                )

                noteRepository.insertNote(note)
                analyticsService.logRecordingSaved(note.id, note.duration)
                
                _uiState.update { it.copy(
                    isLoading = false,
                    error = null
                )}
                
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to save recording: ${e.message}",
                    isLoading = false
                )}
                analyticsService.logRecordingError(e)
            }
        }
    }

    private fun startTranscription() {
        transcriptionJob?.cancel()
        transcriptionJob = viewModelScope.launch {
            try {
                transcriptionService.transcribeStream()
                    .collect { text ->
                        _uiState.update { it.copy(
                            transcription = text,
                            isLoading = false
                        )}
                    }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Transcription error: ${e.message}",
                    isLoading = false
                )}
                analyticsService.logTranscriptionError(e)
            }
        }
    }

    private fun createAudioFile(): File {
        val fileName = "recording_${System.currentTimeMillis()}.m4a"
        return File(context.getExternalFilesDir(null), fileName)
    }

    private fun createMediaRecorder(outputFile: File): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
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

    private fun vibrate(pattern: VibrationPattern) {
        val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (pattern) {
                VibrationPattern.START -> vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                VibrationPattern.PAUSE -> vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                VibrationPattern.RESUME -> vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                VibrationPattern.STOP -> vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 50, 100), -1))
            }
        } else {
            @Suppress("DEPRECATION")
            when (pattern) {
                VibrationPattern.START -> vibrator.vibrate(100)
                VibrationPattern.PAUSE -> vibrator.vibrate(50)
                VibrationPattern.RESUME -> vibrator.vibrate(50)
                VibrationPattern.STOP -> vibrator.vibrate(longArrayOf(0, 100, 50, 100), -1)
            }
        }
    }

    private enum class VibrationPattern {
        START, PAUSE, RESUME, STOP
    }

    fun dismissUpgradePrompt() {
        analyticsService.trackUpgradePromptAction(
            feature = PremiumFeature.TRANSCRIPTION,
            action = "dismiss",
            source = "recording_screen"
        )
        _uiState.update { it.copy(showUpgradePrompt = false) }
    }

    fun navigateToSubscription() {
        analyticsService.trackUpgradePromptAction(
            feature = PremiumFeature.TRANSCRIPTION,
            action = "upgrade",
            source = "recording_screen"
        )
        _uiState.update { it.copy(showUpgradePrompt = false) }
        // Navigation will be handled by the composable
    }

    override fun onCleared() {
        super.onCleared()
        recorder?.release()
        recorder = null
        recordingJob?.cancel()
        transcriptionJob?.cancel()
        if (uiState.value.recordingState == RecordingState.RECORDING) {
            stopRecording()
        }
    }
} 