package com.example.clicknote.service.recording

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import androidx.work.*
import com.example.clicknote.analytics.AnalyticsManager
import com.example.clicknote.domain.repository.CallRecordingRepository
import com.example.clicknote.service.IRecordingService
import com.example.clicknote.service.TranscriptionMode
import com.example.clicknote.service.notification.CallRecordingNotificationService
import com.example.clicknote.service.transcription.TranscriptionManager
import com.example.clicknote.util.audio.AudioEnhancer
import com.example.clicknote.util.ContactUtils
import com.example.clicknote.worker.CallProcessingWorker
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.io.File

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CallRecordingServiceEntryPoint {
    fun repository(): CallRecordingRepository
    fun transcriptionManager(): TranscriptionManager
    fun audioEnhancer(): AudioEnhancer
    fun contactUtils(): ContactUtils
    fun notificationService(): CallRecordingNotificationService
    fun analyticsManager(): AnalyticsManager
}

class CallRecordingService : Service(), IRecordingService {

    private lateinit var repository: CallRecordingRepository
    private lateinit var transcriptionManager: TranscriptionManager
    private lateinit var audioEnhancer: AudioEnhancer
    private lateinit var contactUtils: ContactUtils
    private lateinit var notificationService: CallRecordingNotificationService
    private lateinit var analyticsManager: AnalyticsManager

    private var mediaRecorder: MediaRecorder? = null
    private var recordingStartTime: Long = 0
    private var currentPhoneNumber: String? = null
    private var isIncoming: Boolean = false
    private var recordingJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _isRecording = MutableStateFlow(false)
    override val isRecording: Flow<Boolean> = _isRecording.asStateFlow()

    private val _transcriptionText = MutableStateFlow("")
    override val transcriptionText: Flow<String> = _transcriptionText.asStateFlow()

    private val _amplitude = MutableStateFlow(0f)
    override val amplitude: Flow<Float> = _amplitude.asStateFlow()

    private var transcriptionMode = TranscriptionMode.HYBRID
    private var transcriptionLanguage = "en-US"
    private var recordingDuration: Long = 0L
    private var recordingFilePath: String? = null

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when (state) {
                TelephonyManager.CALL_STATE_RINGING -> {
                    currentPhoneNumber = phoneNumber
                    isIncoming = true
                }
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    if (currentPhoneNumber == null) {
                        currentPhoneNumber = phoneNumber
                        isIncoming = false
                    }
                    CallRecordingForegroundService.start(this@CallRecordingService)
                    startRecording()
                    showRecordingNotification()
                    analyticsManager.trackCallRecordingStarted(
                        phoneNumber = currentPhoneNumber ?: "Unknown",
                        isIncoming = isIncoming
                    )
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    stopRecording()
                    currentPhoneNumber = null
                    CallRecordingForegroundService.stop(this@CallRecordingService)
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            CallRecordingServiceEntryPoint::class.java
        )
        repository = entryPoint.repository()
        transcriptionManager = entryPoint.transcriptionManager()
        audioEnhancer = entryPoint.audioEnhancer()
        contactUtils = entryPoint.contactUtils()
        notificationService = entryPoint.notificationService()
        analyticsManager = entryPoint.analyticsManager()
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val phoneNumber = intent?.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)
        startRecording(phoneNumber)
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRecording(phoneNumber: String?) {
        if (currentPhoneNumber != null) return

        currentPhoneNumber = phoneNumber
        isIncoming = phoneNumber != null
        startRecording()
    }

    private fun startRecording() {
        val outputFile = createOutputFile()
        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(outputFile.absolutePath)
            try {
                prepare()
                start()
                recordingStartTime = System.currentTimeMillis()
                notificationService.showRecordingNotification()
                analyticsManager.trackCallRecordingStarted(
                    phoneNumber = currentPhoneNumber ?: "Unknown",
                    isIncoming = isIncoming
                )
            } catch (e: Exception) {
                e.printStackTrace()
                reset()
                release()
                analyticsManager.trackCallRecordingError(
                    phoneNumber = currentPhoneNumber ?: "Unknown",
                    error = e.message ?: "Unknown error during recording start"
                )
            }
        }
    }

    private fun stopRecording() {
        recordingJob?.cancel()
        mediaRecorder?.apply {
            try {
                stop()
            } catch (e: Exception) {
                e.printStackTrace()
                analyticsManager.trackCallRecordingError(
                    phoneNumber = currentPhoneNumber ?: "Unknown",
                    error = e.message ?: "Unknown error during recording stop"
                )
            } finally {
                reset()
                release()
            }
        }
        mediaRecorder = null

        val endTime = System.currentTimeMillis()
        val duration = endTime - recordingStartTime
        val outputFile = getLatestOutputFile()

        if (outputFile != null && duration > 0) {
            analyticsManager.trackCallRecordingCompleted(
                phoneNumber = currentPhoneNumber ?: "Unknown",
                duration = duration,
                transcriptionLength = 0, // Will be updated by worker
                isIncoming = isIncoming
            )
            processRecording(outputFile, duration)
        }
    }

    private suspend fun processRecording(audioFile: File, duration: Long) {
        try {
            val enhancedFile = audioEnhancer.enhance(audioFile)
            val transcription = transcriptionManager.transcribe(enhancedFile)
            
            val workRequest = OneTimeWorkRequestBuilder<CallProcessingWorker>()
                .setInputData(workDataOf(
                    CallProcessingWorker.KEY_AUDIO_FILE_PATH to enhancedFile.absolutePath,
                    CallProcessingWorker.KEY_PHONE_NUMBER to (currentPhoneNumber ?: "Unknown"),
                    CallProcessingWorker.KEY_TIMESTAMP to recordingStartTime,
                    CallProcessingWorker.KEY_DURATION to duration,
                    CallProcessingWorker.KEY_IS_INCOMING to isIncoming
                ))
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .build()

            WorkManager.getInstance(applicationContext)
                .enqueueUniqueWork(
                    "call_processing_${System.currentTimeMillis()}",
                    ExistingWorkPolicy.REPLACE,
                    workRequest
                )

            analyticsManager.trackCallRecordingCompleted(
                phoneNumber = currentPhoneNumber ?: "Unknown",
                duration = duration,
                transcriptionLength = transcription.length,
                isIncoming = isIncoming
            )
        } catch (e: Exception) {
            analyticsManager.trackCallRecordingError(
                phoneNumber = currentPhoneNumber ?: "Unknown",
                error = e.message ?: "Unknown error during recording processing"
            )
        } finally {
            cleanup()
            stopSelf()
        }
    }

    private fun showRecordingNotification() {
        val formattedNumber = currentPhoneNumber?.let { 
            contactUtils.formatPhoneNumber(it)
        } ?: "Unknown"
        notificationService.showRecordingNotification(formattedNumber, isIncoming)
    }

    private fun createOutputFile(): File {
        val recordingsDir = File(getExternalFilesDir(null), "call_recordings").apply {
            if (!exists()) mkdirs()
        }
        return File(recordingsDir, "call_${System.currentTimeMillis()}.m4a")
    }

    private fun getLatestOutputFile(): File? {
        val recordingsDir = File(getExternalFilesDir(null), "call_recordings")
        return recordingsDir.listFiles()
            ?.maxByOrNull { it.lastModified() }
    }

    private fun cleanup() {
        mediaRecorder?.release()
        mediaRecorder = null
        currentPhoneNumber = null
        isIncoming = false
        recordingJob = null
        notificationService.dismissNotification()
        CallRecordingForegroundService.stop(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        stopRecording()
        serviceScope.cancel()
        cleanup()
    }

    override suspend fun startRecording() {
        startRecording(null)
    }

    override suspend fun stopRecording(): Result<String> = runCatching {
        stopRecording()
        _transcriptionText.value
    }

    override suspend fun pauseRecording() {
        mediaRecorder?.pause()
        _isRecording.value = false
    }

    override suspend fun resumeRecording() {
        mediaRecorder?.resume()
        _isRecording.value = true
    }

    override suspend fun cancelRecording() {
        stopRecording()
        getLatestOutputFile()?.delete()
    }

    override fun getRecordingDuration(): Long = recordingDuration

    override fun getRecordingFilePath(): String? = recordingFilePath

    override suspend fun cleanup() {
        cleanup()
    }

    override suspend fun isRecordingAvailable(): Boolean {
        return true // Implement actual permission and subscription check
    }

    override fun getTranscriptionMode(): TranscriptionMode = transcriptionMode

    override suspend fun setTranscriptionMode(mode: TranscriptionMode) {
        transcriptionMode = mode
    }

    override fun getTranscriptionLanguage(): String = transcriptionLanguage

    override suspend fun setTranscriptionLanguage(language: String) {
        transcriptionLanguage = language
    }
} 