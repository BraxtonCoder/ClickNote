package com.example.clicknote.service

import android.content.Context
import android.util.Log
import android.util.LruCache
import com.example.clicknote.domain.model.TranscriptionSegment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest
import kotlin.math.min

object WhisperLib {
    private const val TAG = "WhisperLib"
    private const val CACHE_SIZE = 100 * 1024 * 1024 // 100MB cache
    private const val CHUNK_SIZE = 16000 * 30 // 30 seconds of audio at 16kHz
    private const val SAMPLE_RATE = 16000

    // Cache for frequently used audio segments
    private val audioCache = LruCache<String, List<TranscriptionSegment>>(CACHE_SIZE)

    // Available language models
    private val availableModels = mapOf(
        "en" to "whisper-tiny-en.tflite",
        "multi" to "whisper-tiny-multi.tflite",
        "large" to "whisper-base-multi.tflite"
    )

    private var currentModel: String = "en"
    private var isModelLoaded = false

    init {
        try {
            System.loadLibrary("whisper_jni")
            isModelLoaded = true
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load native library", e)
            throw ModelInitializationException("Failed to load native library: ${e.message}")
        }
    }

    suspend fun initializeModel(
        context: Context, 
        language: String = "en",
        modelType: ModelType = ModelType.TINY
    ): Long = withContext(Dispatchers.IO) {
        try {
            val modelFileName = getModelFileName(language, modelType)
            val modelFile = File(context.filesDir, modelFileName)

            if (!modelFile.exists() || shouldUpdateModel(modelFile)) {
                copyModelFromAssets(context, modelFileName, modelFile)
            }

            currentModel = language
            createModel(modelFile.absolutePath).also { handle ->
                if (handle == 0L) {
                    throw ModelInitializationException("Failed to create model instance")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing Whisper model", e)
            throw ModelInitializationException("Failed to initialize model: ${e.message}")
        }
    }

    private fun shouldUpdateModel(modelFile: File): Boolean {
        // Check if model is older than 30 days
        return System.currentTimeMillis() - modelFile.lastModified() > 30 * 24 * 60 * 60 * 1000
    }

    private fun getModelFileName(language: String, modelType: ModelType): String {
        return when (modelType) {
            ModelType.TINY -> if (language == "en") "whisper-tiny-en.tflite" else "whisper-tiny-multi.tflite"
            ModelType.BASE -> if (language == "en") "whisper-base-en.tflite" else "whisper-base-multi.tflite"
            ModelType.SMALL -> if (language == "en") "whisper-small-en.tflite" else "whisper-small-multi.tflite"
        }
    }

    private fun copyModelFromAssets(context: Context, modelFileName: String, outputFile: File) {
        try {
            context.assets.open(modelFileName).use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            throw ModelInitializationException("Failed to copy model from assets: ${e.message}")
        }
    }

    suspend fun transcribeAudio(
        handle: Long,
        audioData: ByteArray,
        progressCallback: (Float) -> Unit,
        useCache: Boolean = true
    ): List<TranscriptionSegment> = withContext(Dispatchers.Default) {
        try {
            if (!isModelLoaded) {
                throw TranscriptionException("Model not loaded")
            }

            // Check cache first if enabled
            if (useCache) {
                val cacheKey = calculateAudioHash(audioData)
                audioCache.get(cacheKey)?.let { cached ->
                    Log.d(TAG, "Using cached transcription")
                    return@withContext cached
                }
            }

            // Split audio into optimal chunks for parallel processing
            val chunks = audioData.toList().chunked(CHUNK_SIZE)
            val totalChunks = chunks.size
            val optimalChunkCount = (Runtime.getRuntime().availableProcessors() - 1).coerceAtLeast(1)
            val batchSize = (totalChunks / optimalChunkCount).coerceAtLeast(1)
            
            val segments = mutableListOf<TranscriptionSegment>()
            var currentTime = 0L
            var completedChunks = 0

            // Process chunks in parallel batches
            chunks.chunked(batchSize).forEach { batch ->
                // Process each batch in parallel
                val batchResults = batch.mapIndexed { index, chunk ->
                    async {
                        try {
                            val buffer = ByteBuffer.allocateDirect(chunk.size)
                                .order(ByteOrder.LITTLE_ENDIAN)
                                .put(chunk.toByteArray())
                            buffer.rewind()

                            val timestamp = currentTime + (index * CHUNK_SIZE * 1000L) / (SAMPLE_RATE * 2)
                            transcribeChunk(handle, buffer, timestamp)
                        } catch (e: Exception) {
                            Log.e(TAG, "Error processing chunk", e)
                            emptyList()
                        } finally {
                            completedChunks++
                            progressCallback(completedChunks.toFloat() / totalChunks)
                        }
                    }
                }

                // Await and collect results from the batch
                val batchSegments = batchResults.awaitAll().flatten()
                segments.addAll(batchSegments)
                
                currentTime += (batch.sumOf { it.size } * 1000L) / (SAMPLE_RATE * 2)
            }

            // Post-process segments to ensure continuity
            val processedSegments = postProcessSegments(segments)

            // Cache the result if enabled
            if (useCache && processedSegments.isNotEmpty()) {
                val cacheKey = calculateAudioHash(audioData)
                audioCache.put(cacheKey, processedSegments)
            }

            processedSegments
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing audio", e)
            throw TranscriptionException("Failed to transcribe audio: ${e.message}")
        }
    }

    private fun postProcessSegments(segments: List<TranscriptionSegment>): List<TranscriptionSegment> {
        if (segments.isEmpty()) return segments

        val processed = mutableListOf<TranscriptionSegment>()
        var currentSegment = segments.first()
        
        // Merge segments with small gaps
        segments.drop(1).forEach { segment ->
            val gap = segment.startTime - currentSegment.endTime
            if (gap < 300) { // Less than 300ms gap
                // Merge segments
                currentSegment = currentSegment.copy(
                    text = "${currentSegment.text} ${segment.text}",
                    endTime = segment.endTime
                )
            } else {
                processed.add(currentSegment)
                currentSegment = segment
            }
        }
        processed.add(currentSegment)

        return processed
    }

    private fun calculateAudioHash(audioData: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256")
        return digest.digest(audioData).joinToString("") { "%02x".format(it) }
    }

    suspend fun transcribeStream(
        handle: Long,
        audioBuffer: ByteBuffer,
        timestamp: Long
    ): List<TranscriptionSegment> = withContext(Dispatchers.Default) {
        try {
            if (!isModelLoaded) {
                throw TranscriptionException("Model not loaded")
            }

            // Process stream in smaller chunks for better real-time performance
            val chunkSize = CHUNK_SIZE / 2 // Use smaller chunks for streaming
            val chunks = mutableListOf<ByteBuffer>()
            
            while (audioBuffer.hasRemaining()) {
                val size = min(chunkSize, audioBuffer.remaining())
                val chunk = ByteBuffer.allocateDirect(size)
                    .order(ByteOrder.LITTLE_ENDIAN)
                
                // Copy data to chunk
                val limit = audioBuffer.position() + size
                audioBuffer.limit(limit)
                chunk.put(audioBuffer)
                chunk.rewind()
                
                chunks.add(chunk)
            }

            // Process chunks in parallel
            val results = chunks.mapIndexed { index, chunk ->
                async {
                    val chunkTimestamp = timestamp + (index * chunkSize * 1000L) / (SAMPLE_RATE * 2)
                    transcribeChunk(handle, chunk, chunkTimestamp)
                }
            }.awaitAll()

            // Merge results
            postProcessSegments(results.flatten())
        } catch (e: Exception) {
            Log.e(TAG, "Error transcribing stream", e)
            throw TranscriptionException("Failed to transcribe stream: ${e.message}")
        }
    }

    fun clearCache() {
        audioCache.evictAll()
    }

    fun getCacheSize(): Int = audioCache.size()

    fun isLanguageSupported(language: String): Boolean = availableModels.containsKey(language)

    private external fun createModel(modelPath: String): Long
    private external fun transcribeChunk(handle: Long, buffer: ByteBuffer, timestamp: Long): List<TranscriptionSegment>
    private external fun transcribeWithTimestamps(handle: Long, audioPath: String): Array<NativeSegment>
    private external fun destroyModel(handle: Long)

    enum class ModelType {
        TINY,   // ~75MB
        BASE,   // ~150MB
        SMALL   // ~500MB
    }

    data class NativeSegment(
        val text: String,
        val startTime: Long,
        val endTime: Long
    ) {
        fun toTranscriptionSegment() = TranscriptionSegment(
            text = text,
            startTime = startTime,
            endTime = endTime
        )
    }

    sealed class WhisperException(message: String) : Exception(message) {
        override fun toString(): String = "${this::class.simpleName}: $message"
    }

    class ModelInitializationException(message: String) : WhisperException(message)
    class TranscriptionException(message: String) : WhisperException(message)
    class UnsupportedLanguageException(message: String) : WhisperException(message)
    class CacheException(message: String) : WhisperException(message)
} 