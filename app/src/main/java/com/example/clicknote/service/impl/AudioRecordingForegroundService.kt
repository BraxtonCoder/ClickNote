package com.example.clicknote.service.impl

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.content.Context
import android.os.PowerManager
import androidx.core.app.NotificationManagerCompat
import com.example.clicknote.domain.interfaces.AudioRecordingService
import com.example.clicknote.domain.service.NotificationHandler
import com.example.clicknote.domain.service.NotificationIds
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingForegroundService : Service() {

    @Inject
    lateinit var audioRecordingService: AudioRecordingService

    @Inject
    lateinit var notificationHandler: NotificationHandler

    private var wakeLock: PowerManager.WakeLock? = null

    companion object {
        const val ACTION_START_RECORDING = "com.example.clicknote.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.clicknote.action.STOP_RECORDING"
        const val ACTION_PAUSE_RECORDING = "com.example.clicknote.action.PAUSE_RECORDING"
        const val ACTION_RESUME_RECORDING = "com.example.clicknote.action.RESUME_RECORDING"
    }

    override fun onCreate() {
        super.onCreate()
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ClickNote::AudioRecordingWakeLock"
        ).apply {
            setReferenceCounted(false)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                notificationHandler.showRecordingNotification()
                startForeground(NotificationIds.RECORDING, notificationHandler.createRecordingNotification())
                wakeLock?.acquire(10*60*1000L) // 10 minutes timeout
                audioRecordingService.startRecording()
            }
            ACTION_STOP_RECORDING -> {
                audioRecordingService.stopRecording()
                wakeLock?.release()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
            ACTION_PAUSE_RECORDING -> {
                audioRecordingService.pauseRecording()
            }
            ACTION_RESUME_RECORDING -> {
                audioRecordingService.resumeRecording()
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock?.release()
    }
} 