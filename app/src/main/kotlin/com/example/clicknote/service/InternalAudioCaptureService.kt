package com.example.clicknote.service

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioPlaybackCaptureConfiguration
import android.media.AudioRecord
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.clicknote.R
import com.example.clicknote.di.ServiceNotificationManager
import com.example.clicknote.di.ServicePowerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import javax.inject.Inject

@AndroidEntryPoint
class InternalAudioCaptureService @Inject constructor(
    @ServicePowerManager private val powerManager: PowerManager,
    @ServiceNotificationManager private val notificationManager: NotificationManager
) : Service() {

    @Inject
    lateinit var mediaProjectionManager: MediaProjectionManager

    private var mediaProjection: MediaProjection? = null
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private var wakeLock: PowerManager.WakeLock? = null
    private var outputFile: File? = null

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _recordingState = MutableStateFlow(false)
    val recordingState: StateFlow<Boolean> = _recordingState

    companion object {
        private const val TAG = "InternalAudioCapture"
        private const val NOTIFICATION_ID = 3000
        private const val CHANNEL_ID = "internal_audio_capture_channel"
        private const val CHANNEL_NAME = "Internal Audio Capture"
        
        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_STEREO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        
        // Initialize buffer size in a non-const property
        private val BUFFER_SIZE: Int by lazy {
            AudioRecord.getMinBufferSize(
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT
            )
        }

        const val EXTRA_RESULT_DATA = "result_data"
        const val ACTION_START_RECORDING = "START_INTERNAL_RECORDING"
        const val ACTION_STOP_RECORDING = "STOP_INTERNAL_RECORDING"
        private const val ACTION_RECORDING_INTERNAL_AUDIO = "com.example.clicknote.RECORDING_INTERNAL_AUDIO"
    }

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
        if (_recordingState.value) return

        val projectionManager = applicationContext.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = projectionManager.getMediaProjection(Activity.RESULT_OK, resultData)

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

        _recordingState.value = true
        startRecording()
    }

    private fun startRecording() {
        recordingJob = serviceScope.launch(Dispatchers.IO) {
            val buffer = ByteBuffer.allocateDirect(BUFFER_SIZE)
            audioRecord?.startRecording()

            outputFile?.let { file ->
                FileOutputStream(file).use { outputStream ->
                    while (_recordingState.value) {
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
        _recordingState.value = false
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
        return File(applicationContext.getExternalFilesDir(null), fileName)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Internal audio capture service notification channel"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
        .setContentTitle(getString(R.string.internal_audio_recording))
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