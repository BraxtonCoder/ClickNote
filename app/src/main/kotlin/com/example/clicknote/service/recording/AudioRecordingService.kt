package com.example.clicknote.service.recording

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.clicknote.domain.service.AudioService
import com.example.clicknote.service.RecordingNotificationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingService : Service() {

    @Inject
    lateinit var audioService: AudioService

    @Inject
    lateinit var notificationManager: RecordingNotificationManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isRecording = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, notificationManager.createInitialNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            RecordingNotificationManager.ACTION_RESUME -> startRecording()
            RecordingNotificationManager.ACTION_PAUSE -> pauseRecording()
            RecordingNotificationManager.ACTION_STOP -> stopRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (!isRecording) {
            serviceScope.launch {
                audioService.startRecording(createOutputFile())
                isRecording = true
                notificationManager.showRecordingNotification(isPaused = false)
            }
        }
    }

    private fun pauseRecording() {
        if (isRecording) {
            serviceScope.launch {
                audioService.pauseRecording()
                notificationManager.showRecordingNotification(isPaused = true)
            }
        }
    }

    private fun stopRecording() {
        if (isRecording) {
            serviceScope.launch {
                audioService.stopRecording()
                isRecording = false
                notificationManager.showTranscribingNotification()
                stopSelf()
            }
        }
    }

    private fun createOutputFile(): File {
        val outputDir = File(applicationContext.filesDir, "recordings")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        return File(outputDir, "recording_${System.currentTimeMillis()}.wav")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        notificationManager.cancelNotification()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
    }
} 