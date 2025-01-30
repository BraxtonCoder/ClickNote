package com.example.clicknote.service.impl

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.clicknote.service.AudioRecorder
import com.example.clicknote.service.TranscriptionService
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.repository.PreferencesRepository
import com.example.clicknote.service.RecordingService
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class AudioRecordingServiceImpl : Service() {

    private val audioRecorder: AudioRecorder
    private val transcriptionService: TranscriptionService
    private val noteRepository: NoteRepository
    private val preferencesRepository: PreferencesRepository

    @Inject
    constructor(
        audioRecorder: AudioRecorder,
        transcriptionService: TranscriptionService,
        noteRepository: NoteRepository,
        preferencesRepository: PreferencesRepository
    ) {
        this.audioRecorder = audioRecorder
        this.transcriptionService = transcriptionService
        this.noteRepository = noteRepository
        this.preferencesRepository = preferencesRepository
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var currentRecordingFile: File? = null
    private var isRecording = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> startRecording()
            ACTION_STOP_RECORDING -> stopRecording()
            ACTION_PAUSE_RECORDING -> pauseRecording()
            ACTION_RESUME_RECORDING -> resumeRecording()
        }
        return START_NOT_STICKY
    }

    private fun startRecording() {
        serviceScope.launch {
            if (!canStartRecording()) {
                return@launch
            }
            currentRecordingFile = audioRecorder.startRecording()
            isRecording = true
        }
    }

    private fun stopRecording() {
        serviceScope.launch {
            val audioFile = audioRecorder.stopRecording()
            isRecording = false
            
            val transcription = transcriptionService.transcribe(audioFile)
            val saveAudio = preferencesRepository.getSaveAudio().first()
            
            noteRepository.insertNote(
                content = transcription,
                hasAudio = saveAudio,
                audioPath = if (saveAudio) audioFile else null
            ).getOrThrow()
        }
    }

    private fun pauseRecording() {
        serviceScope.launch {
            audioRecorder.pauseRecording()
        }
    }

    private fun resumeRecording() {
        serviceScope.launch {
            audioRecorder.resumeRecording()
        }
    }

    private suspend fun canStartRecording(): Boolean {
        val isPremium = preferencesRepository.isPremiumUser().first()
        val weeklyCount = preferencesRepository.getWeeklyTranscriptionCount().first()
        return isPremium || weeklyCount < 3
    }

    companion object {
        const val ACTION_START_RECORDING = "com.example.clicknote.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.clicknote.action.STOP_RECORDING"
        const val ACTION_PAUSE_RECORDING = "com.example.clicknote.action.PAUSE_RECORDING"
        const val ACTION_RESUME_RECORDING = "com.example.clicknote.action.RESUME_RECORDING"
    }
} 