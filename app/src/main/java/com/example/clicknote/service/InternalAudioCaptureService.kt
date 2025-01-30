package com.example.clicknote.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.clicknote.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import javax.inject.Inject

@AndroidEntryPoint
class InternalAudioCaptureService : Service() {
    private var mediaProjection: MediaProjection? = null
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var isRecording = false
    private var outputFile: File? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    companion object {
        private const val NOTIFICATION_ID = 3000
        private const val CHANNEL_ID = "internal_audio_capture_channel"
        private const val CHANNEL_NAME = "Internal Audio Capture"
        
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
        )

        const val EXTRA_RESULT_DATA = "result_data"
        const val ACTION_START_RECORDING = "START_INTERNAL_RECORDING"
        const val ACTION_STOP_RECORDING = "STOP_INTERNAL_RECORDING"
    }

    @Inject
    lateinit var powerManager: PowerManager

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> {
                intent.getParcelableExtra<Intent>(EXTRA_RESULT_DATA)?.let { resultData ->
                    startInternalRecording(resultData)
                }
            }
            ACTION_STOP_RECORDING -> stopInternalRecording()
        }
        return START_NOT_STICKY
    }

    private fun startInternalRecording(resultData: Intent) {
        if (isRecording) return

        val projectionManager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(RESULT_OK, resultData)

        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ClickNote::InternalRecordingWakeLock"
        ).apply {
            acquire(10 * 60 * 1000L) // 10 minutes
        }

        val notification = createNotification()
        startForeground(NOTIFICATION_ID, notification)

        outputFile = createOutputFile()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val config = AudioPlaybackCaptureConfiguration.Builder(mediaProjection!!)
                .addMatchingUsage(AudioAttributes.USAGE_MEDIA)
                .addMatchingUsage(AudioAttributes.USAGE_GAME)
                .addMatchingUsage(AudioAttributes.USAGE_UNKNOWN)
                .build()

            audioRecord = AudioRecord.Builder()
                .setAudioFormat(AudioFormat.Builder()
                    .setEncoding(AUDIO_FORMAT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(CHANNEL_CONFIG)
                    .build())
                .setBufferSizeInBytes(BUFFER_SIZE)
                .setAudioPlaybackCaptureConfig(config)
                .build()
        }

        isRecording = true
        startRecording()
    }

    private fun startRecording() {
        recordingJob = serviceScope.launch(Dispatchers.IO) {
            val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
            audioRecord?.startRecording()

            outputFile?.let { file ->
                FileOutputStream(file).use { outputStream ->
                    while (isRecording) {
                        val bytesRead = audioRecord?.read(buffer, BUFFER_SIZE) ?: -1
                        if (bytesRead > 0) {
                            outputStream.channel.write(buffer)
                            buffer.clear()
                        }
                    }
                }
            }
        }
    }

    private fun stopInternalRecording() {
        isRecording = false
        recordingJob?.cancel()
        
        audioRecord?.apply {
            stop()
            release()
        }
        audioRecord = null
        
        mediaProjection?.stop()
        mediaProjection = null
        
        wakeLock?.release()
        wakeLock = null
        
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createOutputFile(): File {
        val fileName = "internal_recording_${System.currentTimeMillis()}.pcm"
        return File(getExternalFilesDir(null), fileName)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Internal audio capture notification channel"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.recording_internal_audio))
        .setSmallIcon(R.drawable.ic_mic)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        stopInternalRecording()
        serviceScope.cancel()
    }
} 