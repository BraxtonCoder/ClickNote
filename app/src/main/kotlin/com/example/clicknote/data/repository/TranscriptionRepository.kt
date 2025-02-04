package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.TranscriptionMetadataDao
import com.example.clicknote.data.entity.TranscriptionMetadata
import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.domain.model.TranscriptionStatus
import com.example.clicknote.service.WhisperService
import com.example.clicknote.service.SpeakerDetectionService
import com.example.clicknote.util.PermissionChecker
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.repository.TranscriptionRepository
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

interface TranscriptionRepository {
    fun getMetadataForNote(noteId: String): Flow<TranscriptionMetadata?>
    fun getMetadataInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TranscriptionMetadata>>
    suspend fun getTranscriptionCount(startDate: LocalDateTime, endDate: LocalDateTime): Int
    suspend fun getAverageConfidenceScore(startDate: LocalDateTime, endDate: LocalDateTime): Float
    suspend fun getAverageProcessingTime(startDate: LocalDateTime, endDate: LocalDateTime): Long
    suspend fun saveTranscription(metadata: TranscriptionMetadata)
    suspend fun updateTranscriptionStatus(noteId: String, status: TranscriptionStatus)
    suspend fun transcribeAudio(noteId: String, audioFile: File): Result<List<TranscriptionSegment>>
    suspend fun transcribeAudioStream(noteId: String, audioData: ByteArray): Result<List<TranscriptionSegment>>
    suspend fun getMetadataById(id: String): TranscriptionMetadata?
    suspend fun deleteById(id: String)
    suspend fun saveAudioFile(noteId: String, audioFile: File): Result<String>
    suspend fun deleteAudioFile(noteId: String): Result<Unit>
}

@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    private val transcriptionMetadataDao: TranscriptionMetadataDao,
    private val whisperService: WhisperService,
    private val speakerDetectionService: SpeakerDetectionService,
    private val permissionChecker: PermissionChecker
) : TranscriptionRepository {

    private val _events = MutableSharedFlow<TranscriptionEvent>()
    override val events: Flow<TranscriptionEvent> = _events.asSharedFlow()

    // Metadata management
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

    // Transcription operations
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
        } else {
            transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.ERROR)
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
        } else {
            transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.ERROR)
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
        }
        result
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun deleteAudioFile(noteId: String): Result<Unit> = try {
        val result = whisperService.deleteAudioFile(noteId)
        if (result.isSuccess) {
            transcriptionMetadataDao.updateStatus(noteId, TranscriptionStatus.AUDIO_DELETED)
        }
        result
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun getAllMetadata(): Flow<List<TranscriptionMetadata>> =
        transcriptionMetadataDao.getAllMetadata()

    fun getMetadataByFolder(folderId: Long): Flow<List<TranscriptionMetadata>> =
        transcriptionMetadataDao.getMetadataByFolder(folderId)

    suspend fun getTranscriptionCount(since: LocalDateTime, isOffline: Boolean): Int =
        transcriptionMetadataDao.getTranscriptionCount(since, isOffline)

    suspend fun getAverageConfidenceScore(noteIds: List<Long>): Float =
        transcriptionMetadataDao.getAverageConfidenceScore(noteIds)

    suspend fun getAverageProcessingTime(model: String): Long =
        transcriptionMetadataDao.getAverageProcessingTime(model)

    suspend fun saveMetadata(metadata: TranscriptionMetadata): Long =
        transcriptionMetadataDao.insert(metadata)

    suspend fun updateMetadata(metadata: TranscriptionMetadata) =
        transcriptionMetadataDao.update(metadata)

    suspend fun deleteMetadata(metadata: TranscriptionMetadata) =
        transcriptionMetadataDao.delete(metadata)

    suspend fun deleteMetadataForNote(noteId: Long) =
        transcriptionMetadataDao.deleteByNoteId(noteId)

    override suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings
    ): Result<String> = try {
        val id = UUID.randomUUID().toString()
        _events.emit(TranscriptionEvent.Started(id))

        val result = whisperService.transcribe(audioData, settings)
        _events.emit(TranscriptionEvent.Completed(id, result))
        Result.success(result.text)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings
    ): Result<String> = try {
        val id = UUID.randomUUID().toString()
        _events.emit(TranscriptionEvent.Started(id))

        val result = whisperService.transcribeFile(file, settings)
        _events.emit(TranscriptionEvent.Completed(id, result))
        Result.success(result.text)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun generateSummary(text: String): Result<String> = try {
        val summary = whisperService.generateSummary(text)
        Result.success(summary)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun detectSpeakers(file: File): Result<List<String>> = try {
        val speakers = speakerDetectionService.detectSpeakers(file)
        Result.success(speakers)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAvailableLanguages(): List<String> =
        whisperService.getAvailableLanguages()

    override fun cancelTranscription() {
        whisperService.cancelTranscription()
    }

    override suspend fun cleanup() {
        whisperService.cleanup()
    }

    override suspend fun saveTranscription(transcriptionResult: TranscriptionResult) {
        val metadata = TranscriptionMetadata(
            id = transcriptionResult.id,
            text = transcriptionResult.text,
            confidence = transcriptionResult.confidence,
            language = transcriptionResult.language,
            duration = transcriptionResult.duration,
            speakers = transcriptionResult.speakers,
            summary = transcriptionResult.summary,
            audioPath = transcriptionResult.audioPath,
            createdAt = LocalDateTime.now(),
            updatedAt = LocalDateTime.now()
        )
        transcriptionMetadataDao.insert(metadata)
    }

    override suspend fun getTranscriptions(): List<TranscriptionResult> =
        transcriptionMetadataDao.getAllMetadata().first().map { it.toDomain() }

    override suspend fun getTranscriptionById(id: String): TranscriptionResult? =
        transcriptionMetadataDao.getMetadataById(id)?.toDomain()

    override suspend fun deleteTranscription(id: String) {
        transcriptionMetadataDao.deleteById(id)
    }

    override suspend fun saveTranscriptionAudio(id: String, audioBytes: ByteArray): String {
        // TODO: Implement audio file saving
        val path = "audio/$id.wav"
        _events.emit(TranscriptionEvent.AudioSaved(id, path))
        return path
    }

    override suspend fun deleteTranscriptionAudio(id: String) {
        // TODO: Implement audio file deletion
        _events.emit(TranscriptionEvent.AudioDeleted(id))
    }

    private fun TranscriptionMetadata.toDomain(): TranscriptionResult =
        TranscriptionResult(
            id = id,
            text = text,
            confidence = confidence,
            language = language,
            duration = duration,
            speakers = speakers,
            segments = emptyList(), // TODO: Implement segment conversion
            summary = summary,
            audioPath = audioPath,
            createdAt = createdAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000,
            updatedAt = updatedAt.toEpochSecond(java.time.ZoneOffset.UTC) * 1000
        )

    enum class ErrorCode {
        PERMISSION_DENIED,
        FILE_NOT_FOUND,
        SPEAKER_DETECTION_FAILED,
        TRANSCRIPTION_FAILED,
        IO_ERROR,
        UNKNOWN_ERROR
    }
} 
} 