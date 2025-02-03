package com.example.clicknote.service.impl

import android.content.Context
import android.media.MediaRecorder
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationManagerCompat
import com.example.clicknote.domain.interfaces.CallRecordingService
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.Speaker
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.service.NotificationService
import com.example.clicknote.util.AudioUtils
import com.example.clicknote.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CallRecordingServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteRepository: NoteRepository,
    private val transcriptionService: TranscriptionCapable,
    private val notificationService: NotificationService,
    private val audioUtils: AudioUtils,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : CallRecordingService {

    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var isRecording = false

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    val recordingState: StateFlow<RecordingState> = _recordingState

    override suspend fun startRecording() {
        if (isRecording) return
        try {
            val outputFile = createOutputFile()
            currentRecordingFile = outputFile
            mediaRecorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            isRecording = true
            _recordingState.value = RecordingState.Recording(LocalDateTime.now())
        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to start recording")
        }
    }

    override suspend fun stopRecording(): String {
        if (!isRecording) return ""
        return try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false
            _recordingState.value = RecordingState.Idle
            currentRecordingFile?.absolutePath ?: ""
        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to stop recording")
            ""
        }
    }

    override suspend fun isRecording(): Boolean = isRecording

    override suspend fun requestPermissions() {
        // Implementation
    }

    override suspend fun hasRequiredPermissions(): Boolean {
        // Implementation
        return false
    }

    override suspend fun getRecordingState(): Boolean = isRecording

    override suspend fun getAudioFilePath(): String? = currentRecordingFile?.absolutePath

    private fun createOutputFile(): File {
        val recordingsDir = File(context.filesDir, "recordings").apply {
            if (!exists()) mkdirs()
        }
        return File(recordingsDir, "recording_${System.currentTimeMillis()}.m4a")
    }

    sealed class RecordingState {
        object Idle : RecordingState()
        data class Recording(
            val startTime: LocalDateTime,
            val phoneNumber: String? = null
        ) : RecordingState()
        data class Error(val message: String) : RecordingState()
    }
}

data class Speaker(
    val id: Int,
    val label: String = "Speaker ${id + 1}"
) 