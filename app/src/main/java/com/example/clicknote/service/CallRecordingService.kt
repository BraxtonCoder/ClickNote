package com.example.clicknote.service

import android.app.Service
import android.content.Intent
import android.media.MediaRecorder
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import com.example.clicknote.domain.model.CallRecording
import com.example.clicknote.domain.repository.CallRecordingRepository
import com.example.clicknote.service.notification.CallRecordingNotificationService
import com.example.clicknote.service.transcription.TranscriptionManager
import com.example.clicknote.util.audio.AudioEnhancer
import com.example.clicknote.util.ContactUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class CallRecordingService : Service() {

    private val repository: CallRecordingRepository
    private val transcriptionManager: TranscriptionManager
    private val audioEnhancer: AudioEnhancer
    private val contactUtils: ContactUtils
    private val notificationService: CallRecordingNotificationService

    @Inject
    constructor(
        repository: CallRecordingRepository,
        transcriptionManager: TranscriptionManager,
        audioEnhancer: AudioEnhancer,
        contactUtils: ContactUtils,
        notificationService: CallRecordingNotificationService
    ) {
        this.repository = repository
        this.transcriptionManager = transcriptionManager
        this.audioEnhancer = audioEnhancer
        this.contactUtils = contactUtils
        this.notificationService = notificationService
    }

    private var mediaRecorder: MediaRecorder? = null
    private var recordingStartTime: Long = 0
    private var currentPhoneNumber: String? = null
    private var isIncoming: Boolean = false
    private var recordingJob: Job? = null
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

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
                    startRecording()
                    showRecordingNotification()
                }
                TelephonyManager.CALL_STATE_IDLE -> {
                    stopRecording()
                    currentPhoneNumber = null
                }
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_CALL_STATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        telephonyManager.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE)
        stopRecording()
        serviceScope.cancel()
        notificationService.dismissNotification()
    }

    override fun onBind(intent: Intent?): IBinder? = null

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
            } catch (e: Exception) {
                e.printStackTrace()
                reset()
                release()
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
            processRecording(outputFile, duration)
        }
    }

    private fun processRecording(audioFile: File, duration: Long) {
        recordingJob = serviceScope.launch {
            try {
                showTranscribingNotification()
                
                // Enhance audio quality
                val enhancedAudioFile = audioEnhancer.enhance(audioFile)
                
                // Get transcription
                val transcription = transcriptionManager.transcribe(enhancedAudioFile)
                
                // Generate summary
                val summary = transcriptionManager.generateSummary(transcription)
                
                // Get contact name
                val contactName = currentPhoneNumber?.let { 
                    contactUtils.getContactName(it)
                }

                // Create and save recording
                val recording = CallRecording.create(
                    phoneNumber = currentPhoneNumber ?: "Unknown",
                    contactName = contactName,
                    timestamp = recordingStartTime,
                    duration = duration,
                    audioFilePath = enhancedAudioFile.absolutePath,
                    transcription = transcription,
                    summary = summary,
                    isIncoming = isIncoming
                )
                
                repository.insertCallRecording(recording)
                
                // Delete original audio file if different from enhanced
                if (audioFile != enhancedAudioFile) {
                    audioFile.delete()
                }
                
                notificationService.dismissNotification()
            } catch (e: Exception) {
                e.printStackTrace()
                notificationService.dismissNotification()
            }
        }
    }

    private fun showRecordingNotification() {
        val formattedNumber = currentPhoneNumber?.let { 
            contactUtils.formatPhoneNumber(it)
        } ?: "Unknown"
        notificationService.showRecordingNotification(formattedNumber, isIncoming)
    }

    private fun showTranscribingNotification() {
        val formattedNumber = currentPhoneNumber?.let { 
            contactUtils.formatPhoneNumber(it)
        } ?: "Unknown"
        notificationService.showTranscribingNotification(formattedNumber)
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
} 