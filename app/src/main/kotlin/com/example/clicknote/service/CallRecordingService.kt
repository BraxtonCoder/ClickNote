package com.example.clicknote.service

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.domain.service.AnalyticsService
import com.example.clicknote.domain.service.BillingService
import com.example.clicknote.service.notification.CallRecordingNotificationService
import com.example.clicknote.service.transcription.TranscriptionManager
import com.example.clicknote.service.AudioEnhancer
import com.example.clicknote.util.ContactUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject
import java.util.UUID

@AndroidEntryPoint
class CallRecordingService : Service() {

    @Inject
    lateinit var transcriptionService: TranscriptionService

    @Inject
    lateinit var noteRepository: NoteRepository

    @Inject
    lateinit var analyticsService: AnalyticsService

    @Inject
    lateinit var billingService: BillingService

    @Inject
    lateinit var transcriptionManager: TranscriptionManager

    @Inject
    lateinit var audioEnhancer: AudioEnhancer

    @Inject
    lateinit var contactUtils: ContactUtils

    @Inject
    lateinit var notificationService: CallRecordingNotificationService

    private var mediaRecorder: MediaRecorder? = null
    private var recordingFile: File? = null
    private var isRecording = false
    private var currentPhoneNumber: String? = null
    private var startTime: Long = 0
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val phoneStateListener = object : PhoneStateListener() {
        override fun onCallStateChanged(state: Int, phoneNumber: String?) {
            when (state) {
                TelephonyManager.CALL_STATE_OFFHOOK -> {
                    currentPhoneNumber = phoneNumber
                    startRecording(phoneNumber)
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    stopRecording()
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
                startRecording(phoneNumber)
            }
            ACTION_STOP_RECORDING -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording(phoneNumber: String?) {
        if (isRecording) return

        serviceScope.launch {
            if (!billingService.canMakeTranscription()) {
                // Handle subscription limit reached
                return@launch
            }

            try {
                recordingFile = File(applicationContext.cacheDir, "call_${System.currentTimeMillis()}.m4a")
                mediaRecorder = MediaRecorder().apply {
                    setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setOutputFile(recordingFile?.path)
                    prepare()
                    start()
                }
                isRecording = true
                startTime = System.currentTimeMillis()
                analyticsService.trackCallRecordingStarted(phoneNumber ?: "unknown", false)
            } catch (e: Exception) {
                analyticsService.trackCallRecordingError(phoneNumber ?: "unknown", e.message ?: "Unknown error")
                cleanup()
            }
        }
    }

    private fun stopRecording() {
        if (!isRecording) return

        serviceScope.launch {
            try {
                mediaRecorder?.apply {
                    stop()
                    release()
                }
                mediaRecorder = null
                isRecording = false

                recordingFile?.let { file ->
                    processRecording(file)
                }
            } catch (e: Exception) {
                analyticsService.trackCallRecordingError(currentPhoneNumber ?: "unknown", e.message ?: "Unknown error")
            } finally {
                cleanup()
            }
        }
    }

    private suspend fun processRecording(file: File) {
        try {
            // Transcribe audio
            val transcriptionResult = transcriptionService.transcribeFile(
                file,
                com.example.clicknote.domain.model.TranscriptionSettings(
                    noteId = UUID.randomUUID().toString(),
                    enableSpeakerDetection = true
                )
            ).getOrThrow()

            // Create note
            val note = Note(
                id = UUID.randomUUID().toString(),
                title = "Call with ${currentPhoneNumber ?: "Unknown"} - ${LocalDateTime.now()}",
                content = transcriptionResult.text,
                createdAt = LocalDateTime.now(),
                modifiedAt = LocalDateTime.now(),
                source = NoteSource.CALL,
                hasAudio = true,
                audioPath = file.path,
                duration = ((System.currentTimeMillis() - startTime) / 1000).toInt(),
                transcriptionLanguage = transcriptionResult.language,
                speakerCount = transcriptionResult.speakers.size
            )

            // Save note
            noteRepository.insertNote(note)

            // Track analytics
            analyticsService.trackCallRecordingCompleted(
                phoneNumber = currentPhoneNumber ?: "unknown",
                duration = System.currentTimeMillis() - startTime,
                transcriptionLength = transcriptionResult.text.length,
                isIncoming = false
            )

            // Consume transcription
            billingService.consumeTranscription()

        } catch (e: Exception) {
            analyticsService.trackCallRecordingError(currentPhoneNumber ?: "unknown", e.message ?: "Unknown error")
        }
    }

    private fun cleanup() {
        mediaRecorder?.release()
        mediaRecorder = null
        recordingFile?.delete()
        recordingFile = null
        isRecording = false
        currentPhoneNumber = null
        startTime = 0
    }

    override fun onDestroy() {
        super.onDestroy()
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        serviceScope.cancel()
        cleanup()
        notificationService.dismissNotification()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START_RECORDING = "com.example.clicknote.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.clicknote.action.STOP_RECORDING"
        const val EXTRA_PHONE_NUMBER = "phone_number"
    }
} 