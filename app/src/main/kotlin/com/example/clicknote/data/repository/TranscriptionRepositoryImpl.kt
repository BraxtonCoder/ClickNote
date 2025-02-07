package com.example.clicknote.data.repository

import android.content.Context
import com.example.clicknote.data.dao.TranscriptionMetadataDao
import com.example.clicknote.data.entity.TranscriptionMetadata
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.repository.TranscriptionRepository
import com.example.clicknote.domain.service.SpeakerDetectionService
import com.example.clicknote.domain.service.SummaryService
import com.example.clicknote.service.WhisperService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.io.File
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val metadataDao: TranscriptionMetadataDao,
    private val whisperService: WhisperService,
    private val speakerDetectionService: SpeakerDetectionService,
    private val summaryService: SummaryService
) : TranscriptionRepository {

    private val _events = MutableSharedFlow<TranscriptionEvent>()
    override val events: Flow<TranscriptionEvent> = _events.asSharedFlow()

    override fun getMetadataForNote(noteId: String): Flow<TranscriptionMetadata?> = flow {
        val metadata = metadataDao.getMetadataForNote(noteId)
        emit(metadata)
    }

    override fun getMetadataInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TranscriptionMetadata>> = flow {
        val metadata = metadataDao.getMetadataInDateRange(startDate, endDate)
        emit(metadata)
    }

    override suspend fun getTranscriptionCount(): Int =
        metadataDao.getTranscriptionCount()

    override suspend fun getAverageConfidenceScore(): Float =
        metadataDao.getAverageConfidence() ?: 0f

    override suspend fun getAverageProcessingTime(): Long =
        metadataDao.getAverageProcessingTime() ?: 0L

    override suspend fun saveTranscription(metadata: TranscriptionMetadata) {
        metadataDao.insert(metadata)
    }

    override suspend fun updateTranscriptionStatus(noteId: String, status: TranscriptionStatus) {
        metadataDao.updateStatus(noteId, status.name)
    }

    override suspend fun transcribeAudio(audioData: ByteArray, settings: TranscriptionSettings): Result<String> = runCatching {
        val startTime = System.currentTimeMillis()
        val tempFile = File.createTempFile("audio", ".wav", context.cacheDir)
        tempFile.writeBytes(audioData)
        
        updateTranscriptionStatus(settings.noteId, TranscriptionStatus.PROCESSING)
        
        val result = whisperService.transcribe(tempFile)
        val processingTime = System.currentTimeMillis() - startTime
        
        result.fold(
            onSuccess = { text ->
                updateTranscriptionStatus(settings.noteId, TranscriptionStatus.COMPLETED)
                metadataDao.updateProcessingTime(settings.noteId, processingTime)
                text
            },
            onFailure = { error ->
                updateTranscriptionStatus(settings.noteId, TranscriptionStatus.ERROR)
                throw error
            }
        )
    }

    override suspend fun transcribeFile(file: File, settings: TranscriptionSettings): Result<String> = runCatching {
        updateTranscriptionStatus(settings.noteId, TranscriptionStatus.PROCESSING)
        val startTime = System.currentTimeMillis()
        
        val result = whisperService.transcribe(file)
        val processingTime = System.currentTimeMillis() - startTime
        
        result.fold(
            onSuccess = { text ->
                updateTranscriptionStatus(settings.noteId, TranscriptionStatus.COMPLETED)
                metadataDao.updateProcessingTime(settings.noteId, processingTime)
                text
            },
            onFailure = { error ->
                updateTranscriptionStatus(settings.noteId, TranscriptionStatus.ERROR)
                throw error
            }
        )
    }

    override suspend fun generateSummary(text: String): Result<String> = runCatching {
        summaryService.generateQuickSummary(text).getOrThrow()
    }

    override suspend fun detectSpeakers(file: File): Result<List<String>> = runCatching {
        speakerDetectionService.detectSpeakers(file).getOrThrow()
    }

    override suspend fun getAvailableLanguages(): List<String> {
        return listOf(
            "en", "es", "fr", "de", "it", "pt", "nl", "ru", "ja", "ko", "zh",
            "ar", "hi", "tr", "pl", "vi", "th", "id", "cs", "da", "fi", "el"
        )
    }

    override fun cancelTranscription() {
        whisperService.cancelTranscription()
    }

    override suspend fun cleanup() {
        context.cacheDir.listFiles()?.forEach { file ->
            if (file.name.startsWith("audio") && file.extension == "wav") {
                file.delete()
            }
        }
    }

    override suspend fun getMetadataById(id: String): TranscriptionMetadata? =
        metadataDao.getMetadataById(id)

    override suspend fun deleteById(id: String) {
        metadataDao.deleteById(id)
    }

    override suspend fun saveAudioFile(noteId: String, audioFile: File): Result<String> = runCatching {
        val fileName = "audio_$noteId.wav"
        val destFile = File(context.filesDir, fileName)
        audioFile.copyTo(destFile, overwrite = true)
        updateTranscriptionStatus(noteId, TranscriptionStatus.AUDIO_SAVED)
        destFile.absolutePath
    }

    override suspend fun deleteAudioFile(noteId: String): Result<Unit> = runCatching {
        val fileName = "audio_$noteId.wav"
        val file = File(context.filesDir, fileName)
        if (file.exists()) {
            file.delete()
        }
        updateTranscriptionStatus(noteId, TranscriptionStatus.AUDIO_DELETED)
    }
}