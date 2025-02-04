package com.example.clicknote.data.repository

import com.example.clicknote.domain.model.TranscriptionResult
import com.example.clicknote.domain.repository.TranscriptionRepository
import connectors.default.DefaultConnector
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TranscriptionRepositoryImpl @Inject constructor(
    private val connector: DefaultConnector,
    private val json: Json
) : TranscriptionRepository {
    
    companion object {
        private const val COLLECTION_TRANSCRIPTIONS = "transcriptions"
    }
    
    override suspend fun saveTranscription(transcriptionResult: TranscriptionResult) {
        connector.executeMutation(
            collection = COLLECTION_TRANSCRIPTIONS,
            document = transcriptionResult.id,
            data = transcriptionResult,
            serializer = TranscriptionResult.serializer()
        )
    }
    
    override suspend fun getTranscriptions(): List<TranscriptionResult> {
        return connector.executeQuery(
            collection = COLLECTION_TRANSCRIPTIONS,
            deserializer = ListSerializer(TranscriptionResult.serializer())
        )
    }
    
    override suspend fun getTranscriptionById(id: String): TranscriptionResult {
        return connector.executeQuery(
            collection = COLLECTION_TRANSCRIPTIONS,
            queryParams = mapOf("id" to id),
            deserializer = TranscriptionResult.serializer()
        )
    }
    
    override suspend fun deleteTranscription(id: String) {
        connector.deleteDocument(COLLECTION_TRANSCRIPTIONS, id)
    }
    
    override suspend fun saveTranscriptionAudio(id: String, audioBytes: ByteArray): String {
        return connector.uploadFile("transcriptions/$id/audio.mp3", audioBytes)
    }
    
    override suspend fun deleteTranscriptionAudio(id: String) {
        connector.deleteFile("transcriptions/$id/audio.mp3")
    }
} 