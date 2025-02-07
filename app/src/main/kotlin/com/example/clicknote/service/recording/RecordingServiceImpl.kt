package com.example.clicknote.service.recording

import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.IBinder
import android.os.PowerManager
import com.example.clicknote.R
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.NoteSource
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.service.RecordingNotificationManager
import com.example.clicknote.service.TranscriptionManager
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.qualifiers.ApplicationContext
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
class RecordingServiceImpl @Inject constructor(
    private val notificationManager: RecordingNotificationManager,
    private val userPreferences: UserPreferencesDataStore,
    private val noteRepository: NoteRepository,
    private val transcriptionManager: TranscriptionManager,
    @ApplicationContext private val appContext: Context
) : Service(), IRecordingService {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var recordingJob: Job? = null
    private var audioRecord: AudioRecord? = null
    private var audioFile: File? = null
    private var isRecording = false
    private var wakeLock: PowerManager.WakeLock? = null

    private val _waveformData = MutableStateFlow(FloatArray(0))
    override val waveformData: StateFlow<FloatArray> = _waveformData

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, notificationManager.createInitialNotification())
    }

    override suspend fun startRecording() {
        if (isRecording) return

        serviceScope.launch {
            try {
                audioFile = createAudioFile()
                setupAudioRecorder()
                startRecordingProcess()
            } catch (e: Exception) {
                stopRecording()
            }
        }
    }

    override suspend fun pauseRecording() {
        if (!isRecording) return
        recordingJob?.cancel()
        audioRecord?.stop()
    }

    override suspend fun stopRecording() {
        if (!isRecording) return

        serviceScope.launch {
            try {
                isRecording = false
                recordingJob?.cancel()
                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null

                audioFile?.let { file ->
                    if (file.exists() && file.length() > 0) {
                        createNoteFromRecording(file)
                    }
                }

                cleanup()
                stopForeground(true)
                stopSelf()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override suspend fun isRecording(): Boolean = isRecording

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

    private fun startRecordingProcess() {
        isRecording = true
        audioRecord?.startRecording()

        recordingJob = serviceScope.launch(Dispatchers.IO) {
            val buffer = ShortArray(BUFFER_SIZE)
            val audioOutput = FileOutputStream(audioFile)

            try {
                while (isRecording) {
                    val readSize = audioRecord?.read(buffer, 0, BUFFER_SIZE) ?: -1
                    if (readSize > 0) {
                        val waveform = calculateWaveform(buffer, readSize)
                        _waveformData.value = waveform
                        writeAudioData(buffer, readSize, audioOutput)
                    }
                }
            } finally {
                audioOutput.close()
            }
        }
    }

    private fun calculateWaveform(buffer: ShortArray, size: Int): FloatArray {
        val waveform = FloatArray(size)
        for (i in 0 until size) {
            waveform[i] = buffer[i].toFloat() / Short.MAX_VALUE
        }
        return waveform
    }

    private fun writeAudioData(buffer: ShortArray, size: Int, output: FileOutputStream) {
        val bytes = ByteArray(size * 2)
        for (i in 0 until size) {
            bytes[i * 2] = (buffer[i].toInt() and 0xFF).toByte()
            bytes[i * 2 + 1] = (buffer[i].toInt() shr 8 and 0xFF).toByte()
        }
        output.write(bytes)
    }

    private fun createAudioFile(): File {
        val dir = File(appContext.filesDir, "recordings").apply {
            if (!exists()) mkdirs()
        }
        return File(dir, "recording_${System.currentTimeMillis()}.wav")
    }

    private suspend fun createNoteFromRecording(file: File) {
        val note = Note(
            id = java.util.UUID.randomUUID().toString(),
            title = "Recording ${LocalDateTime.now()}",
            content = "",
            createdAt = LocalDateTime.now(),
            modifiedAt = LocalDateTime.now(),
            source = NoteSource.VOICE,
            hasAudio = true,
            audioPath = file.absolutePath
        )

        noteRepository.insertNote(note)
        transcriptionManager.transcribeAudio(file, note.id)
    }

    private fun cleanup() {
        recordingJob?.cancel()
        audioRecord?.release()
        audioRecord = null
        wakeLock?.release()
        wakeLock = null
    }

    override fun onDestroy() {
        super.onDestroy()
        cleanup()
        serviceScope.cancel()
    }

    companion object {
        private const val NOTIFICATION_ID = 1001
        private const val SAMPLE_RATE = 44100
        private const val BUFFER_SIZE = 2048
    }
} 