package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.TranscriptionMetadataDao
import com.example.clicknote.data.entity.TranscriptionMetadata
import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.domain.model.TranscriptionStatus
import com.example.clicknote.domain.model.TranscriptionEvent
import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.domain.model.TranscriptionSettings
import com.example.clicknote.service.WhisperService
import com.example.clicknote.service.SpeakerDetectionService
import com.example.clicknote.util.PermissionChecker
import com.example.clicknote.domain.repository.TranscriptionRepository
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    private val transcriptionMetadataDao: TranscriptionMetadataDao,
    private val whisperService: WhisperService,
    private val speakerDetectionService: SpeakerDetectionService,
    private val permissionChecker: PermissionChecker
) : TranscriptionRepository {

    private val _events = MutableSharedFlow<TranscriptionEvent>()
    val events: Flow<TranscriptionEvent> = _events.asSharedFlow()

    override fun getMetadataForNote(noteId: String): Flow<TranscriptionMetadata?> =
        transcriptionMetadataDao.getMetadataForNote(noteId)

    override fun getMetadataInDateRange(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Flow<List<TranscriptionMetadata>> =
        transcriptionMetadataDao.getMetadataInDateRange(startDate, endDate)

    override suspend fun getTranscriptionCount(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Int =
        transcriptionMetadataDao.getTranscriptionCount(startDate, endDate)

    override suspend fun getAverageConfidenceScore(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Float =
        transcriptionMetadataDao.getAverageConfidenceScore(startDate, endDate)

    override suspend fun getAverageProcessingTime(
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): Long =
        transcriptionMetadataDao.getAverageProcessingTime(startDate, endDate) ?: 0L

    override suspend fun saveTranscription(metadata: TranscriptionMetadata) {
        transcriptionMetadataDao.insert(metadata)
    }

    override suspend fun updateTranscriptionStatus(noteId: String, status: TranscriptionStatus) {
        transcriptionMetadataDao.updateStatus(noteId, status)
    }

    override suspend fun transcribeAudio(
        noteId: String,
        audioFile: File
    ): Result<List<TranscriptionSegment>> = try {
        val startTime = System.currentTimeMillis()
        val result = whisperService.transcribeAudio(audioFile)
        val endTime = System.currentTimeMillis()

        if (result.isSuccess) {
            transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.COMPLETED)
            transcriptionMetadataDao.updateProcessingTime(noteId, endTime - startTime)
            _events.emit(TranscriptionEvent.Completed(noteId, result.getOrNull()))
        } else {
            transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.ERROR)
            _events.emit(TranscriptionEvent.Failed(noteId, result.exceptionOrNull()?.message))
        }

        result
    } catch (e: Exception) {
        transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.ERROR)
        Result.failure(e)
    }

    override suspend fun transcribeAudioStream(
        noteId: String,
        audioData: ByteArray
    ): Result<List<TranscriptionSegment>> = try {
        val startTime = System.currentTimeMillis()
        val result = whisperService.transcribeAudioStream(audioData)
        val endTime = System.currentTimeMillis()

        if (result.isSuccess) {
            transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.COMPLETED)
            transcriptionMetadataDao.updateProcessingTime(noteId, endTime - startTime)
            _events.emit(TranscriptionEvent.Completed(noteId, result.getOrNull()))
        } else {
            transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.ERROR)
            _events.emit(TranscriptionEvent.Failed(noteId, result.exceptionOrNull()?.message))
        }

        result
    } catch (e: Exception) {
        transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.ERROR)
        Result.failure(e)
    }

    override suspend fun getMetadataById(id: String): TranscriptionMetadata? =
        transcriptionMetadataDao.getMetadataById(id)

    override suspend fun deleteById(id: String) {
        transcriptionMetadataDao.deleteById(id)
    }

    override suspend fun saveAudioFile(noteId: String, audioFile: File): Result<String> = try {
        val result = whisperService.saveAudioFile(noteId, audioFile)
        if (result.isSuccess) {
            transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.AUDIO_SAVED)
            _events.emit(TranscriptionEvent.AudioSaved(noteId, result.getOrNull() ?: ""))
        }
        result
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAudioFile(noteId: String): Result<Unit> = try {
        val result = whisperService.deleteAudioFile(noteId)
        if (result.isSuccess) {
            transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.AUDIO_DELETED)
            _events.emit(TranscriptionEvent.AudioDeleted(noteId))
        }
        result
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun saveTranscriptionResult(result: TranscriptionResult) {
        val metadata = TranscriptionMetadata(
            noteId = result.noteId,
            text = result.text,
            confidence = result.confidence,
            language = result.language,
            duration = result.duration,
            speakers = result.speakers,
            summary = result.summary,
            audioPath = result.audioPath,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now(),
            status = TranscriptionStatus.COMPLETED,
            processingTime = result.processingTime,
            wordCount = result.text.split(" ").size,
            confidenceScore = result.confidence
        )
        transcriptionMetadataDao.insert(metadata)
    }

    private fun TranscriptionMetadata.toResult(): TranscriptionResult =
        TranscriptionResult(
            noteId = noteId,
            text = text,
            confidence = confidence,
            language = language,
            duration = duration,
            speakers = speakers,
            segments = emptyList(), // TODO: Implement segment conversion
            summary = summary,
            audioPath = audioPath,
            processingTime = processingTime,
            createdAt = createdAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
            updatedAt = updatedAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        )
}