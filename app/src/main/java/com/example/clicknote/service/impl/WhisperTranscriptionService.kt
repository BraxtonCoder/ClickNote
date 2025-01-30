package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.service.TranscriptionService
import com.example.clicknote.service.TranscriptionSegment
import com.example.clicknote.service.Speaker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.tensorflow.lite.task.audio.classifier.AudioClassifier
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import javax.inject.Inject
import kotlin.math.min

class WhisperTranscriptionService @Inject constructor(
    @ApplicationContext private val context: Context
) : TranscriptionService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var classifier: AudioClassifier? = null
    private var isTranscribing = false
    
    private val _transcriptionProgress = MutableStateFlow(0f)
    override val transcriptionProgress: Flow<Float> = _transcriptionProgress.asStateFlow()
    
    private val _detectedSpeakers = MutableStateFlow<List<Speaker>>(emptyList())
    override val detectedSpeakers: Flow<List<Speaker>> = _detectedSpeakers.asStateFlow()

    private val speakerMap = mutableMapOf<String, Speaker>()
    private var nextSpeakerId = 1

    init {
        initializeModel()
    }

    private fun initializeModel() {
        try {
            // Load the Whisper model
            classifier = AudioClassifier.createFromFile(context, MODEL_FILE)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun transcribe(audioFile: File, detectSpeakers: Boolean): String = withContext(Dispatchers.Default) {
        try {
            val segments = mutableListOf<TranscriptionSegment>()
            val audioData = loadAudioFile(audioFile)
            val totalChunks = (audioData.size / CHUNK_SIZE) + 1
            
            for ((index, chunk) in audioData.chunked(CHUNK_SIZE).withIndex()) {
                val buffer = ByteBuffer.allocateDirect(CHUNK_SIZE)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .put(chunk.toByteArray())
                
                val results = classifier?.classify(buffer)
                results?.maxByOrNull { it.score }?.let { result ->
                    val speakerId = if (detectSpeakers) {
                        detectSpeaker(chunk)
                    } else null
                    
                    segments.add(
                        TranscriptionSegment(
                            text = result.label,
                            speakerId = speakerId,
                            startTime = (index * CHUNK_SIZE_MS).toLong(),
                            endTime = (min((index + 1) * CHUNK_SIZE_MS, audioData.size / SAMPLE_RATE) * 1000).toLong(),
                            confidence = result.score
                        )
                    )
                }
                
                _transcriptionProgress.value = (index + 1).toFloat() / totalChunks
            }
            
            return@withContext mergeSegments(segments)
        } catch (e: Exception) {
            throw TranscriptionException("Failed to transcribe audio", e)
        } finally {
            _transcriptionProgress.value = 0f
        }
    }

    override fun startRealtimeTranscription(): Flow<TranscriptionSegment> = flow {
        isTranscribing = true
        val buffer = ByteBuffer.allocateDirect(CHUNK_SIZE)
            .order(ByteOrder.LITTLE_ENDIAN)
        
        while (isTranscribing) {
            // Process audio in real-time
            classifier?.classify(buffer)?.maxByOrNull { it.score }?.let { result ->
                emit(
                    TranscriptionSegment(
                        text = result.label,
                        startTime = System.currentTimeMillis(),
                        endTime = System.currentTimeMillis() + CHUNK_SIZE_MS,
                        confidence = result.score
                    )
                )
            }
            delay(CHUNK_SIZE_MS.toLong())
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun stopRealtimeTranscription() {
        isTranscribing = false
    }

    private fun detectSpeaker(audioChunk: List<Byte>): String {
        // Simplified speaker detection using audio characteristics
        val signature = calculateAudioSignature(audioChunk)
        
        return speakerMap.entries.firstOrNull { 
            isSimilarSignature(signature, it.key)
        }?.value?.id ?: createNewSpeaker(signature)
    }

    private fun calculateAudioSignature(audioChunk: List<Byte>): String {
        // Simplified audio fingerprinting
        // In a real implementation, this would use more sophisticated techniques
        return audioChunk.chunked(1024)
            .map { it.average() }
            .joinToString(",")
    }

    private fun isSimilarSignature(sig1: String, sig2: String): Boolean {
        // Simplified signature comparison
        // In a real implementation, this would use more sophisticated comparison
        val values1 = sig1.split(",").map { it.toDouble() }
        val values2 = sig2.split(",").map { it.toDouble() }
        
        return values1.zip(values2)
            .map { abs(it.first - it.second) }
            .average() < SIGNATURE_THRESHOLD
    }

    private fun createNewSpeaker(signature: String): String {
        val speakerId = "speaker_$nextSpeakerId"
        val speaker = Speaker(
            id = speakerId,
            label = "Person $nextSpeakerId"
        )
        speakerMap[signature] = speaker
        nextSpeakerId++
        
        _detectedSpeakers.value = speakerMap.values.toList()
        return speakerId
    }

    private fun mergeSegments(segments: List<TranscriptionSegment>): String {
        return segments.groupBy { it.speakerId }
            .map { (speakerId, speakerSegments) ->
                val speaker = speakerMap[speakerId]?.label ?: "Unknown Speaker"
                speakerSegments.joinToString("\n") { segment ->
                    "$speaker: ${segment.text}"
                }
            }
            .joinToString("\n\n")
    }

    private fun loadAudioFile(file: File): List<Byte> {
        return file.readBytes().toList()
    }

    companion object {
        private const val MODEL_FILE = "whisper-small-en.tflite"
        private const val CHUNK_SIZE = 16000 * 30 // 30 seconds of audio at 16kHz
        private const val CHUNK_SIZE_MS = 30000 // 30 seconds in milliseconds
        private const val SAMPLE_RATE = 16000
        private const val SIGNATURE_THRESHOLD = 0.1
    }
}

class TranscriptionException(message: String, cause: Throwable? = null) : Exception(message, cause)

private fun abs(value: Double): Double = if (value < 0) -value else value 