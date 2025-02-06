package com.example.clicknote.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.clicknote.MainActivity
import com.example.clicknote.R
import com.example.clicknote.di.ServiceNotificationManager
import com.example.clicknote.di.ServicePowerManager
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.service.transcription.TranscriptionManager
import com.example.clicknote.util.NotificationHelper
import com.example.clicknote.domain.model.RecordingState
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.*
import java.io.File
import java.time.LocalDateTime
import java.util.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@EntryPoint
@InstallIn(SingletonComponent::class)
interface RecordingServiceEntryPoint {
    fun userPreferences(): UserPreferencesDataStore
    fun noteRepository(): NoteRepository
    fun transcriptionManager(): TranscriptionManager
    fun notificationHelper(): NotificationHelper
}

@AndroidEntryPoint
class RecordingService @Inject constructor(
    @ServicePowerManager private val powerManager: PowerManager,
    @ServiceNotificationManager private val notificationManager: NotificationManager,
    private val userPreferences: UserPreferencesDataStore,
    private val noteRepository: NoteRepository,
    private val transcriptionManager: TranscriptionManager,
    private val notificationHelper: NotificationHelper
) : Service() {
    private var wakeLock: PowerManager.WakeLock? = null
    private var audioRecord: AudioRecord? = null
    private var recordingJob: Job? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var audioFile: File? = null
    private var isRecording = false

    private val _waveformData = MutableStateFlow<List<Float>>(emptyList())
    val waveformData: StateFlow<List<Float>> = _waveformData.asStateFlow()

    companion object {
        const val ACTION_START_RECORDING = "com.example.clicknote.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.clicknote.action.STOP_RECORDING"
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "recording_channel"
        private const val CHANNEL_NAME = "Recording"

        private const val SAMPLE_RATE = 44100
        private const val CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO
        private const val AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT
        private const val BUFFER_SIZE = AudioRecord.getMinBufferSize(
            SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT
        )
    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> startRecording()
            ACTION_STOP_RECORDING -> stopRecording()
        }
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRecording() {
        if (isRecording) return

        serviceScope.launch {
            val canRecord = checkRecordingLimit()
            if (!canRecord) {
                // Notify user about recording limit
                return@launch
            }

            wakeLock = powerManager.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK,
                "ClickNote::RecordingWakeLock"
            ).apply {
                acquire(10 * 60 * 1000L) // 10 minutes
            }

            val notification = createNotification()
            startForeground(NOTIFICATION_ID, notification)

            val shouldSaveAudio = userPreferences.saveAudio.first()
            if (shouldSaveAudio) {
                audioFile = createAudioFile()
            }

            audioRecord = AudioRecord(
                MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE,
                CHANNEL_CONFIG,
                AUDIO_FORMAT,
                BUFFER_SIZE
            )

            isRecording = true
            startAudioRecording()
        }
    }

    private fun startAudioRecording() {
        recordingJob = serviceScope.launch {
            val buffer = ShortArray(BUFFER_SIZE)
            audioRecord?.startRecording()

            try {
                while (isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                    if (readSize > 0) {
                        processAudioData(buffer, readSize)
                        audioFile?.let { file ->
                            writeAudioToFile(file, buffer, readSize)
                        }
                    }
                }
            } finally {
                audioRecord?.stop()
            }
        }
    }

    private suspend fun processAudioData(buffer: ShortArray, readSize: Int) {
        // Calculate waveform data
        val waveform = calculateWaveform(buffer, readSize)
        _waveformData.emit(waveform)

        // Send audio data to transcription manager
        transcriptionManager.processAudioData(buffer, readSize)
    }

    private fun calculateWaveform(buffer: ShortArray, readSize: Int): List<Float> {
        val waveform = mutableListOf<Float>()
        val samplesPerPoint = readSize / 50 // Adjust this value to control waveform resolution
        
        for (i in 0 until readSize step samplesPerPoint) {
            var sum = 0f
            for (j in 0 until samplesPerPoint) {
                if (i + j < readSize) {
                    sum += Math.abs(buffer[i + j].toFloat()) / Short.MAX_VALUE
                }
            }
            waveform.add(sum / samplesPerPoint)
        }
        return waveform
    }

    private suspend fun checkRecordingLimit(): Boolean {
        val isPremium = userPreferences.isPremium.first()
        if (isPremium) return true

        val weeklyCount = userPreferences.weeklyTranscriptionCount.first()
        return weeklyCount < 3
    }

    private fun stopRecording() {
        if (!isRecording) return

        serviceScope.launch {
            isRecording = false
            recordingJob?.cancel()
            
            val transcription = transcriptionManager.finalizeTranscription()
            val audioPath = audioFile?.absolutePath

            val note = Note(
                id = UUID.randomUUID().toString(),
                content = transcription,
                audioPath = audioPath,
                createdAt = LocalDateTime.now(),
                updatedAt = LocalDateTime.now()
            )

            noteRepository.insertNote(note)
            userPreferences.incrementWeeklyTranscriptionCount()

            cleanup()
            stopForeground(true)
            stopSelf()
        }
    }

    private fun cleanup() {
        audioRecord?.release()
        audioRecord = null
        wakeLock?.release()
        wakeLock = null
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
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createNotification() = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle(getString(R.string.recording_start))
        .setSmallIcon(R.drawable.ic_mic)
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .build()

    private fun createAudioFile(): File {
        val dir = File(applicationContext.filesDir, "recordings")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return File(dir, "recording_${System.currentTimeMillis()}.pcm")
    }

    private suspend fun writeAudioToFile(file: File, buffer: ShortArray, readSize: Int) {
        withContext(Dispatchers.IO) {
            file.outputStream().use { output ->
                for (i in 0 until readSize) {
                    output.write(buffer[i].toByte())
                }
            }
        }
    }
}

interface IRecordingService {
    /**
     * Current recording state
     */
    val isRecording: Flow<Boolean>

    /**
     * Current transcription text
     */
    val transcriptionText: Flow<String>

    /**
     * Current audio amplitude for waveform visualization
     */
    val amplitude: Flow<Float>

    /**
     * Start recording
     */
    suspend fun startRecording()

    /**
     * Stop recording and return the transcription result
     */
    suspend fun stopRecording(): Result<String>

    /**
     * Pause recording
     */
    suspend fun pauseRecording()

    /**
     * Resume recording
     */
    suspend fun resumeRecording()

    /**
     * Cancel recording and discard any recorded data
     */
    suspend fun cancelRecording()

    /**
     * Get the current recording duration in milliseconds
     */
    fun getRecordingDuration(): Long

    /**
     * Get the current recording file path
     */
    fun getRecordingFilePath(): String?

    /**
     * Clean up resources
     */
    suspend fun cleanup()

    /**
     * Check if recording is available (based on permissions and subscription status)
     */
    suspend fun isRecordingAvailable(): Boolean

    /**
     * Get the current transcription mode (online/offline)
     */
    fun getTranscriptionMode(): TranscriptionMode

    /**
     * Set the transcription mode
     */
    suspend fun setTranscriptionMode(mode: TranscriptionMode)

    /**
     * Get the current transcription language
     */
    fun getTranscriptionLanguage(): String

    /**
     * Set the transcription language
     */
    suspend fun setTranscriptionLanguage(language: String)
}

enum class TranscriptionMode {
    ONLINE,
    OFFLINE,
    HYBRID
}

interface RecordingService {
    suspend fun startRecording()
    suspend fun stopRecording(): Result<File>
    suspend fun pauseRecording()
    suspend fun resumeRecording()
    fun getRecordingState(): Flow<RecordingState>
    fun getAmplitude(): Flow<Float>
    fun getDuration(): Flow<Long>
    suspend fun cleanup()
} 