package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.TranscriptionMetadataDao
import com.example.clicknote.data.entity.TranscriptionMetadata
import com.example.clicknote.domain.service.TranscriptionService
import com.example.clicknote.domain.service.SpeakerDetectionService
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

@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    private val transcriptionMetadataDao: TranscriptionMetadataDao,
    private val transcriptionService: TranscriptionService,
    private val speakerDetectionService: SpeakerDetectionService,
    private val permissionChecker: PermissionChecker
) : TranscriptionRepository {

    private val _events = MutableSharedFlow<TranscriptionEvent>()
    override val events: Flow<TranscriptionEvent> = _events.asSharedFlow()

    override suspend fun transcribeAudio(
        audioData: ByteArray,
        settings: TranscriptionSettings
    ): Result<String> = try {
        val id = UUID.randomUUID().toString()
        _events.emit(TranscriptionEvent.Started(id))

        val result = transcriptionService.transcribeAudio(audioData, settings)
        result.onSuccess { transcriptionResult ->
            _events.emit(TranscriptionEvent.Completed(id, transcriptionResult))
        }.onFailure { error ->
            _events.emit(TranscriptionEvent.Failed(id, error))
        }
        result.map { it.text }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun transcribeFile(
        file: File,
        settings: TranscriptionSettings
    ): Result<String> = try {
        val id = UUID.randomUUID().toString()
        _events.emit(TranscriptionEvent.Started(id))

        val result = transcriptionService.transcribeFile(file, settings)
        result.onSuccess { transcriptionResult ->
            _events.emit(TranscriptionEvent.Completed(id, transcriptionResult))
        }.onFailure { error ->
            _events.emit(TranscriptionEvent.Failed(id, error))
        }
        result.map { it.text }
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun generateSummary(text: String): Result<String> = try {
        transcriptionService.generateSummary(text)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun detectSpeakers(file: File): Result<List<String>> = try {
        speakerDetectionService.detectSpeakers(file)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun getAvailableLanguages(): List<String> =
        transcriptionService.getAvailableLanguages()

    override fun cancelTranscription() {
        transcriptionService.cancelTranscription()
    }

    override suspend fun cleanup() {
        transcriptionService.cleanup()
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
}