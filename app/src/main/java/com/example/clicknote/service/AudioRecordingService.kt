package com.example.clicknote.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.os.PowerManager
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
class AudioRecordingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val performanceMonitor: PerformanceMonitor,
    private val amplitudeProcessor: AmplitudeProcessor,
    private val notificationHandler: NotificationHandler,
    private val recordingManager: RecordingManager
) : Service() {
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var outputFile: File? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var errorCount = 0
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    
    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    
    private val _audioAmplitudes = MutableStateFlow<List<Float>>(emptyList())
    val audioAmplitudes: StateFlow<List<Float>> = _audioAmplitudes.asStateFlow()

    private val _isRecording = MutableStateFlow(false)
    val isRecording: Flow<Boolean> = _isRecording.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    val amplitude: Flow<Float> = _amplitude.asStateFlow()

    companion object {
        private const val SAMPLE_RATE = 16000 // Hz (required for Whisper)
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
        )
        private const val MAX_ERROR_COUNT = 3
        const val ACTION_START_RECORDING = "START_RECORDING"
        const val ACTION_STOP_RECORDING = "STOP_RECORDING"
        const val ACTION_PAUSE_RECORDING = "PAUSE_RECORDING"
        const val ACTION_RESUME_RECORDING = "RESUME_RECORDING"
    }

    override fun onCreate() {
        super.onCreate()
        acquireWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> startRecording()
            ACTION_STOP_RECORDING -> stopRecording()
            ACTION_PAUSE_RECORDING -> pauseRecording()
            ACTION_RESUME_RECORDING -> resumeRecording()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRecording() {
        if (_recordingState.value is RecordingState.Recording) return
        
        performanceMonitor.startOperation("recording_session")
        
        try {
            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            ).apply {
                if (state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("Failed to initialize AudioRecord")
                }
            }

            outputFile = createOutputFile()
            val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(ByteOrder.LITTLE_ENDIAN)

            audioRecord?.startRecording()
            _recordingState.value = RecordingState.Recording
            _isRecording.value = true
            
            notificationHandler.showRecordingNotification(true)
            
            recordingJob = serviceScope.launch(Dispatchers.IO) {
                val outputStream = FileOutputStream(outputFile)
                
                try {
                    while (_recordingState.value is RecordingState.Recording) {
                        val bytesRead = audioRecord?.read(buffer, BUFFER_SIZE) ?: -1
                        if (bytesRead > 0) {
                            // Write raw audio data to file
                            outputStream.channel.write(buffer)
                            buffer.clear()
                            
                            // Process amplitude data
                            val amplitude = calculateAmplitude(buffer)
                            amplitudeProcessor.processAmplitude(amplitude)
                            _amplitude.value = amplitude
                        }
                    }
                } catch (e: Exception) {
                    handleRecordingError(e)
                } finally {
                    outputStream.close()
                    buffer.clear()
                }
            }
        } catch (e: Exception) {
            handleRecordingError(e)
        }
    }

    private fun stopRecording() {
        performanceMonitor.endOperation("recording_session")
        
        try {
            recordingJob?.cancel()
            
            audioRecord?.apply {
                stop()
                release()
            }
            audioRecord = null
            
            _recordingState.value = RecordingState.Processing
            _isRecording.value = false
            
            notificationHandler.showRecordingNotification(false)
            
            // Clean up amplitude processor
            amplitudeProcessor.reset()
            
            outputFile?.let { file ->
                serviceScope.launch {
                    stopForeground(STOP_FOREGROUND_REMOVE)
                    stopSelf()
                }
            }
        } catch (e: Exception) {
            handleRecordingError(e)
        } finally {
            performanceMonitor.logMetrics()
        }
    }

    private fun pauseRecording() {
        performanceMonitor.startOperation("pause_recording")
        
        try {
            audioRecord?.stop()
            _recordingState.value = RecordingState.Paused
            _isRecording.value = false
            notificationHandler.showRecordingNotification(false)
        } catch (e: Exception) {
            handleRecordingError(e)
        } finally {
            performanceMonitor.endOperation("pause_recording")
        }
    }

    private fun resumeRecording() {
        performanceMonitor.startOperation("resume_recording")
        
        try {
            audioRecord?.startRecording()
            _recordingState.value = RecordingState.Recording
            _isRecording.value = true
            notificationHandler.showRecordingNotification(true)
        } catch (e: Exception) {
            handleRecordingError(e)
        } finally {
            performanceMonitor.endOperation("resume_recording")
        }
    }

    private fun handleRecordingError(error: Exception) {
        errorCount++
        performanceMonitor.startOperation("error_handling")
        
        try {
            if (errorCount >= MAX_ERROR_COUNT) {
                _recordingState.value = RecordingState.Error(error.message ?: "Recording failed")
                cleanup()
            }
        } finally {
            performanceMonitor.endOperation("error_handling")
        }
    }

    private fun createOutputFile(): File {
        val recordingsDir = File(context.getExternalFilesDir(null), "recordings").apply { mkdirs() }
        return File(recordingsDir, "recording_${System.currentTimeMillis()}.pcm")
    }

    private fun calculateAmplitude(buffer: ByteBuffer): Float {
        var sum = 0.0
        buffer.position(0)
        val shorts = buffer.asShortBuffer()
        var readSize = 0
        
        while (shorts.hasRemaining() && readSize < BUFFER_SIZE / 2) {
            val sample = shorts.get()
            sum += sample * sample
            readSize++
        }
        
        return if (readSize > 0) {
            kotlin.math.sqrt(sum / readSize).toFloat()
        } else {
            0f
        }
    }

    private fun acquireWakeLock() {
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ClickNote::RecordingWakeLock"
        ).apply {
            acquire(10*60*1000L /*10 minutes*/)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
    }

    private fun cleanup() {
        recordingJob?.cancel()
        wakeLock?.release()
        amplitudeProcessor.reset()
        performanceMonitor.logMetrics()
    }
}

sealed class RecordingState {
    object Idle : RecordingState()
    object Recording : RecordingState()
    object Paused : RecordingState()
    object Processing : RecordingState()
    data class Error(val message: String) : RecordingState()
} 