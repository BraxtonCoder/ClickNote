package com.example.clicknote.data.repository

import com.example.clicknote.data.dao.TranscriptionMetadataDao
import com.example.clicknote.data.entity.TranscriptionMetadata
import com.example.clicknote.service.WhisperService
import com.example.clicknote.service.SpeakerDetectionService
import com.example.clicknote.util.PermissionChecker
import kotlinx.coroutines.flow.*
import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepository @Inject constructor(
    private val transcriptionMetadataDao: TranscriptionMetadataDao,
    private val whisperService: WhisperService,
    private val speakerDetectionService: SpeakerDetectionService,
    private val permissionChecker: PermissionChecker
) {
    // Metadata management
    fun getMetadataForNote(noteId: Long): Flow<TranscriptionMetadata?> =
        transcriptionMetadataDao.getMetadataForNote(noteId)

    fun getAllMetadata(): Flow<List<TranscriptionMetadata>> =
        transcriptionMetadataDao.getAllMetadata()

    fun getMetadataInDateRange(startDate: LocalDateTime, endDate: LocalDateTime): Flow<List<TranscriptionMetadata>> =
        transcriptionMetadataDao.getMetadataInDateRange(startDate, endDate)

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

    // Transcription operations
    fun transcribeAudio(
        audioFile: File,
        noteId: Long,
        language: String = "en",
        detectSpeakers: Boolean = true
    ): Flow<TranscriptionResult> = flow {
        try {
            // Check permissions
            if (!permissionChecker.hasRecordAudioPermission()) {
                emit(TranscriptionResult.Error(
                    code = ErrorCode.PERMISSION_DENIED,
                    message = "Microphone permission not granted"
                ))
                return@flow
            }

            if (!audioFile.exists()) {
                emit(TranscriptionResult.Error(
                    code = ErrorCode.FILE_NOT_FOUND,
                    message = "Audio file not found"
                ))
                return@flow
            }

            val startTime = System.currentTimeMillis()
            var speakerCount = 1
            var speakerSegments = emptyList<SpeakerDetectionService.SpeakerSegment>()

            // Detect speakers if requested
            if (detectSpeakers) {
                speakerDetectionService.detectSpeakers(audioFile)
                    .collect { result ->
                        when (result) {
                            is SpeakerDetectionService.DetectionResult.Success -> {
                                speakerCount = result.speakerCount
                                speakerSegments = result.segments
                            }
                            is SpeakerDetectionService.DetectionResult.Error -> {
                                emit(TranscriptionResult.Error(
                                    code = ErrorCode.SPEAKER_DETECTION_FAILED,
                                    message = "Speaker detection failed: ${result.message}"
                                ))
                                return@collect
                            }
                        }
                    }
            }
            
            whisperService.transcribeAudio(audioFile)
                .collect { result ->
                    when (result) {
                        is WhisperService.TranscriptionResult.Success -> {
                            val processingTime = System.currentTimeMillis() - startTime
                            val metadata = TranscriptionMetadata(
                                noteId = noteId,
                                language = language,
                                speakerCount = speakerCount,
                                duration = audioFile.length(),
                                wordCount = result.text.split(" ").size,
                                confidenceScore = result.confidence ?: 1.0f,
                                processingTime = processingTime,
                                model = "whisper-tiny-en",
                                isOffline = true
                            )
                            saveMetadata(metadata)
                            emit(TranscriptionResult.Success(
                                text = result.text,
                                speakerSegments = speakerSegments
                            ))
                        }
                        is WhisperService.TranscriptionResult.Error -> {
                            emit(TranscriptionResult.Error(
                                code = ErrorCode.TRANSCRIPTION_FAILED,
                                message = result.message
                            ))
                        }
                    }
                }
        } catch (e: IOException) {
            emit(TranscriptionResult.Error(
                code = ErrorCode.IO_ERROR,
                message = "Error reading audio file: ${e.message}"
            ))
        } catch (e: Exception) {
            emit(TranscriptionResult.Error(
                code = ErrorCode.UNKNOWN_ERROR,
                message = "Error during transcription: ${e.message}"
            ))
        }
    }

    val transcriptionProgress: StateFlow<Float> = whisperService.transcriptionProgress

    enum class ErrorCode {
        PERMISSION_DENIED,
        FILE_NOT_FOUND,
        SPEAKER_DETECTION_FAILED,
        TRANSCRIPTION_FAILED,
        IO_ERROR,
        UNKNOWN_ERROR
    }
}

sealed class TranscriptionResult {
    data class Success(
        val text: String,
        val speakerSegments: List<SpeakerDetectionService.SpeakerSegment> = emptyList()
    ) : TranscriptionResult()
    
    data class Error(
        val code: TranscriptionRepository.ErrorCode,
        val message: String
    ) : TranscriptionResult()
} 