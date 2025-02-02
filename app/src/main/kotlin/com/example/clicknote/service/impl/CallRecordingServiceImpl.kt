package com.example.clicknote.service.impl

import android.content.Context
import android.media.MediaRecorder
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationManagerCompat
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.Speaker
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.service.TranscriptionCapable
import com.example.clicknote.domain.service.PerformanceMonitor
import com.example.clicknote.service.CallRecordingService
import com.example.clicknote.service.NotificationService
import com.example.clicknote.util.AudioUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
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
    private val audioUtils: AudioUtils
) : CallRecordingService {

    private var mediaRecorder: MediaRecorder? = null
    private var currentRecordingFile: File? = null
    private var isRecording = false
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    override val recordingState: StateFlow<RecordingState> = _recordingState

    override fun onCallScreened(call: Call) {
        when (call.state) {
            Call.STATE_ACTIVE -> startRecording(call)
            Call.STATE_DISCONNECTED -> stopRecording()
            else -> {} // Handle other states if needed
        }
    }

    private fun startRecording(call: Call) {
        if (isRecording) return

        try {
            val outputFile = createOutputFile()
            currentRecordingFile = outputFile

            mediaRecorder = MediaRecorder(context).apply {
                setAudioSource(MediaRecorder.AudioSource.VOICE_CALL)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setAudioChannels(2) // Stereo for better speaker separation
                setAudioSamplingRate(44100)
                setAudioEncodingBitRate(128000)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }

            isRecording = true
            _recordingState.value = RecordingState.Recording(
                startTime = LocalDateTime.now(),
                phoneNumber = call.details.handle?.schemeSpecificPart
            )

            // Show recording notification
            notificationService.showCallRecordingNotification()

        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to start recording")
        }
    }

    private fun stopRecording() {
        if (!isRecording) return

        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
            mediaRecorder = null
            isRecording = false

            // Process the recording
            currentRecordingFile?.let { file ->
                processRecording(file)
            }

            _recordingState.value = RecordingState.Idle
            notificationService.cancelCallRecordingNotification()

        } catch (e: Exception) {
            _recordingState.value = RecordingState.Error(e.message ?: "Failed to stop recording")
        }
    }

    private fun processRecording(audioFile: File) {
        coroutineScope.launch {
            try {
                // Show transcribing notification
                notificationService.showTranscribingNotification()

                // Detect speakers and transcribe
                val speakerSegments = audioUtils.detectSpeakers(audioFile.absolutePath)
                val transcription = transcriptionService.transcribeAudio(
                    audioFile.absolutePath,
                    detectSpeakers = true
                )

                // Create note with speaker information
                val note = Note(
                    id = audioFile.nameWithoutExtension,
                    title = "Call Recording - ${LocalDateTime.now()}",
                    content = formatTranscriptionWithSpeakers(transcription, speakerSegments),
                    timestamp = LocalDateTime.now(),
                    audioPath = audioFile.absolutePath,
                    type = Note.Type.CALL,
                    duration = audioUtils.getAudioDuration(audioFile.absolutePath)
                )

                // Save note
                noteRepository.saveNote(note)

                // Show completion notification
                notificationService.showTranscriptionCompleteNotification(note)

            } catch (e: Exception) {
                _recordingState.value = RecordingState.Error(e.message ?: "Failed to process recording")
                notificationService.showTranscriptionErrorNotification()
            } finally {
                notificationService.cancelTranscribingNotification()
            }
        }
    }

    private fun createOutputFile(): File {
        val recordingsDir = File(context.filesDir, "call_recordings").apply {
            if (!exists()) mkdirs()
        }
        return File(recordingsDir, "call_${System.currentTimeMillis()}.m4a")
    }

    private fun formatTranscriptionWithSpeakers(
        transcription: String,
        speakerSegments: List<Pair<Speaker, String>>
    ): String {
        return buildString {
            speakerSegments.forEach { (speaker, text) ->
                appendLine("${speaker.label}: $text")
                appendLine()
            }
        }
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