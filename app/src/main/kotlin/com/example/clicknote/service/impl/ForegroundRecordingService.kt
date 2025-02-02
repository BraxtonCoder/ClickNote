package com.example.clicknote.service.impl

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.example.clicknote.MainActivity
import com.example.clicknote.R
import com.example.clicknote.service.RecordingManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ForegroundRecordingService : Service() {

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "recording_channel"
        private const val CHANNEL_NAME = "Recording"

        const val ACTION_START_RECORDING = "com.example.clicknote.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.clicknote.action.STOP_RECORDING"
        const val ACTION_PAUSE_RECORDING = "com.example.clicknote.action.PAUSE_RECORDING"
        const val ACTION_RESUME_RECORDING = "com.example.clicknote.action.RESUME_RECORDING"
    }

    @Inject
    lateinit var recordingManager: RecordingManager

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var recordingJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
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
        startForeground(NOTIFICATION_ID, createNotification())
        recordingJob = scope.launch {
            recordingManager.startRecording()
            recordingManager.recordingDuration.collectLatest { duration ->
                updateNotification(duration)
            }
        }
    }

    private fun stopRecording() {
        scope.launch {
            recordingManager.stopRecording()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun pauseRecording() {
        scope.launch {
            recordingManager.pauseRecording()
            updateNotification(recordingManager.recordingDuration.value, isPaused = true)
        }
    }

    private fun resumeRecording() {
        scope.launch {
            recordingManager.resumeRecording()
            updateNotification(recordingManager.recordingDuration.value)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Recording notification channel"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification(duration: Long = 0L, isPaused: Boolean = false) =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.recording_notification_title))
            .setContentText(formatDuration(duration))
            .setSmallIcon(R.drawable.ic_mic)
            .setOngoing(true)
            .setShowWhen(false)
            .setUsesChronometer(true)
            .setContentIntent(createPendingIntent())
            .addAction(createStopAction())
            .apply {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    addAction(createPauseResumeAction(isPaused))
                }
            }
            .build()

    private fun updateNotification(duration: Long, isPaused: Boolean = false) {
        val notification = createNotification(duration, isPaused)
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun createPendingIntent(): PendingIntent {
        val intent = Intent(this, MainActivity::class.java)
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createStopAction(): NotificationCompat.Action {
        val intent = Intent(this, ForegroundRecordingService::class.java).apply {
            action = ACTION_STOP_RECORDING
        }
        val pendingIntent = PendingIntent.getService(
            this,
            1,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(
            R.drawable.ic_stop,
            getString(R.string.stop),
            pendingIntent
        )
    }

    private fun createPauseResumeAction(isPaused: Boolean): NotificationCompat.Action {
        val intent = Intent(this, ForegroundRecordingService::class.java).apply {
            action = if (isPaused) ACTION_RESUME_RECORDING else ACTION_PAUSE_RECORDING
        }
        val pendingIntent = PendingIntent.getService(
            this,
            2,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Action(
            if (isPaused) R.drawable.ic_play else R.drawable.ic_pause,
            getString(if (isPaused) R.string.resume else R.string.pause),
            pendingIntent
        )
    }

    private fun formatDuration(duration: Long): String {
        val seconds = (duration / 1000) % 60
        val minutes = (duration / (1000 * 60)) % 60
        val hours = duration / (1000 * 60 * 60)
        return when {
            hours > 0 -> String.format("%02d:%02d:%02d", hours, minutes, seconds)
            else -> String.format("%02d:%02d", minutes, seconds)
        }
    }
} 