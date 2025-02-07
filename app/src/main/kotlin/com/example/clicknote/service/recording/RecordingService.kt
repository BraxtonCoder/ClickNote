package com.example.clicknote.service.recording

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.clicknote.R
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.service.RecordingNotificationManager
import com.example.clicknote.service.TranscriptionManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import javax.inject.Inject

@AndroidEntryPoint
class RecordingService : Service() {

    @Inject
    lateinit var notificationManager: RecordingNotificationManager

    @Inject
    lateinit var userPreferences: UserPreferencesDataStore

    @Inject
    lateinit var noteRepository: NoteRepository

    @Inject
    lateinit var transcriptionManager: TranscriptionManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var recordingJob: Job? = null
    private var isRecording = false
    private var audioFile: File? = null
    private var audioRecord: AudioRecord? = null
    private var wakeLock: PowerManager.WakeLock? = null

    private val _waveformData = MutableStateFlow<FloatArray>(FloatArray(0))
    val waveformData: StateFlow<FloatArray> = _waveformData

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(
            RecordingNotificationManager.NOTIFICATION_ID,
            notificationManager.createInitialNotification()
        )
        setupWakeLock()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            RecordingNotificationManager.ACTION_RESUME -> {
                serviceScope.launch { startRecording() }
            }
            RecordingNotificationManager.ACTION_PAUSE -> {
                serviceScope.launch { pauseRecording() }
            }
            RecordingNotificationManager.ACTION_STOP -> {
                serviceScope.launch { stopRecording() }
            }
        }
        return START_NOT_STICKY
    }

    private fun setupWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "ClickNote::RecordingWakeLock"
        )
    }

    private suspend fun startRecording() {
        if (!isRecording) {
            audioFile = createOutputFile()
            setupAudioRecorder()
            
            wakeLock?.acquire(10 * 60 * 1000L) // 10 minutes
            
            audioRecord?.startRecording()
            isRecording = true
            
            notificationManager.showRecordingNotification(isPaused = false)
            
            startRecordingJob()
        }
    }

    private fun setupAudioRecorder() {
        val bufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            SAMPLE_RATE,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )
    }

    private fun startRecordingJob() {
        recordingJob = serviceScope.launch {
            val buffer = ShortArray(BUFFER_SIZE)
            val outputStream = FileOutputStream(audioFile)

            try {
                while (isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: 0
                    if (readSize > 0) {
                        // Convert shorts to bytes and write to file
                        val bytes = ShortArray(readSize).also { 
                            System.arraycopy(buffer, 0, it, 0, readSize)
                        }.toByteArray()
                        outputStream.write(bytes)
                        
                        // Update waveform
                        updateWaveform(buffer, readSize)
                    }
                }
            } finally {
                outputStream.close()
            }
        }
    }

    private fun updateWaveform(buffer: ShortArray, size: Int) {
        val waveform = FloatArray(size) { i ->
            buffer[i].toFloat() / Short.MAX_VALUE
        }
        _waveformData.value = waveform
    }

    private suspend fun pauseRecording() {
        if (isRecording) {
            isRecording = false
            audioRecord?.stop()
            recordingJob?.cancel()
            notificationManager.showRecordingNotification(isPaused = true)
        }
    }

    private suspend fun stopRecording() {
        if (isRecording) {
            isRecording = false
            recordingJob?.cancel()
            audioRecord?.stop()
            
            val file = audioFile
            if (file != null && file.exists() && file.length() > 0) {
                val note = Note(
                    id = java.util.UUID.randomUUID().toString(),
                    title = "Recording ${LocalDateTime.now()}",
                    content = "",
                    createdAt = LocalDateTime.now(),
                    modifiedAt = LocalDateTime.now(),
                    source = NoteSource.VOICE,
                    audioPath = file.absolutePath,
                    hasAudio = true
                )
                
                noteRepository.insertNote(note)
                transcriptionManager.transcribeAudio(file, note.id)
            }
            
            cleanup()
            stopForeground(true)
            stopSelf()
        }
    }

    private fun cleanup() {
        audioRecord?.release()
        audioRecord = null
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun createOutputFile(): File {
        val outputDir = File(applicationContext.filesDir, "recordings")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }
        return File(outputDir, "recording_${System.currentTimeMillis()}.wav")
    }

    private fun ShortArray.toByteArray(): ByteArray {
        val bytes = ByteArray(size * 2)
        for (i in indices) {
            bytes[i * 2] = (this[i].toInt() and 0xff).toByte()
            bytes[i * 2 + 1] = (this[i].toInt() shr 8).toByte()
        }
        return bytes
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
        serviceScope.cancel()
    }

    companion object {
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
    }
}

interface RecordingService {
    val waveformData: StateFlow<FloatArray>
    suspend fun startRecording()
    suspend fun pauseRecording()
    suspend fun stopRecording()
    suspend fun isRecording(): Boolean
} 