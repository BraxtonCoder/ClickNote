package com.example.clicknote.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.clicknote.analytics.AnalyticsManager
import com.example.clicknote.domain.model.CallRecording
import com.example.clicknote.domain.repository.CallRecordingRepository
import com.example.clicknote.service.AudioEnhancer
import com.example.clicknote.service.transcription.TranscriptionManager
import com.example.clicknote.util.ContactUtils
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.io.File

@HiltWorker
class CallProcessingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: CallRecordingRepository,
    private val transcriptionManager: TranscriptionManager,
    private val audioEnhancer: AudioEnhancer,
    private val contactUtils: ContactUtils,
    private val analyticsManager: AnalyticsManager
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val audioFilePath = inputData.getString(KEY_AUDIO_FILE_PATH) ?: return Result.failure()
        val phoneNumber = inputData.getString(KEY_PHONE_NUMBER) ?: return Result.failure()
        val timestamp = inputData.getLong(KEY_TIMESTAMP, 0)
        val duration = inputData.getLong(KEY_DURATION, 0)
        val isIncoming = inputData.getBoolean(KEY_IS_INCOMING, false)

        return try {
            val audioFile = File(audioFilePath)
            
            analyticsManager.trackTranscriptionStarted(phoneNumber)
            
            setProgress(workDataOf(KEY_PROGRESS to "Enhancing audio"))
            val enhancedAudioFile = audioEnhancer.enhanceAudioFile(audioFile)
            
            setProgress(workDataOf(KEY_PROGRESS to "Transcribing"))
            val transcription = transcriptionManager.transcribe(enhancedAudioFile)
            
            setProgress(workDataOf(KEY_PROGRESS to "Generating summary"))
            val summary = transcriptionManager.generateSummary(transcription)
            
            val contactName = contactUtils.getContactName(phoneNumber)

            val recording = CallRecording.create(
                phoneNumber = phoneNumber,
                contactName = contactName,
                timestamp = timestamp,
                duration = duration,
                audioFilePath = enhancedAudioFile.absolutePath,
                transcription = transcription,
                summary = summary,
                isIncoming = isIncoming
            )
            
            repository.insertCallRecording(recording)
            
            analyticsManager.trackTranscriptionCompleted(
                phoneNumber = phoneNumber,
                duration = duration,
                transcriptionLength = transcription.length
            )
            
            if (audioFile != enhancedAudioFile) {
                audioFile.delete()
            }
            
            Result.success()
        } catch (e: Exception) {
            analyticsManager.trackTranscriptionError(
                phoneNumber = phoneNumber,
                error = e.message ?: "Unknown error"
            )
            Result.failure()
        }
    }

    companion object {
        const val KEY_AUDIO_FILE_PATH = "audio_file_path"
        const val KEY_PHONE_NUMBER = "phone_number"
        const val KEY_TIMESTAMP = "timestamp"
        const val KEY_DURATION = "duration"
        const val KEY_IS_INCOMING = "is_incoming"
        const val KEY_PROGRESS = "progress"
    }
} 