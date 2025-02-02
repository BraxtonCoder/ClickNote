package com.example.clicknote.service

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.clicknote.analytics.AnalyticsTracker
import com.example.clicknote.data.SharedPreferences
import com.example.clicknote.ml.VoiceEmbeddingModel
import com.example.clicknote.util.AudioFeatureExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.io.File
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class MLSpeakerDetectionService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioFeatureExtractor: AudioFeatureExtractor,
    private val analyticsTracker: AnalyticsTracker,
    private val persistentProfiles: SharedPreferences
) {
    private var interpreter: Interpreter? = null
    private var embeddingModel: MappedByteBuffer? = null
    private val modelName = "voice_embedding_model.tflite"
    private val embeddingDimension = 256
    private val minSegmentDuration = 1.0
    private val maxSpeakers = 10
    private var similarityThreshold = 0.82f
    private val minSimilarityThreshold = 0.70f
    private val maxSimilarityThreshold = 0.95f
    private val adaptiveThresholdStep = 0.01f
    private val transitionThreshold = 0.25f
    private val speakerOverlapWindow = 0.75
    private val minOverlapDuration = 0.2
    private val maxOverlapGap = 0.3

    private val cache = LruCache<String, FloatArray>(200)
    private val profiles = mutableMapOf<Int, SpeakerProfile>()
    private val realtimeBuffer = RingBuffer<Float>(16000 * 2)  // 2 seconds buffer
    private val realtimeSegments = mutableListOf<SpeakerSegment>()
    private var lastProcessedTime = 0L
    private val processingInterval = 500L  // Process every 500ms
    private val realtimeThreshold = 0.80f  // Slightly lower threshold for real-time
    private val _realtimeSpeakers = MutableStateFlow<List<SpeakerSegment>>(emptyList())
    val realtimeSpeakers: StateFlow<List<SpeakerSegment>> = _realtimeSpeakers.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    data class SpeakerProfile(
        val id: Int,
        val name: String? = null,
        val embeddings: MutableList<FloatArray>,
        var totalDuration: Double = 0.0,
        var averageConfidence: Float = 0f,
        var lastUpdated: Long = System.currentTimeMillis(),
        var speakerCharacteristics: Map<String, Float> = emptyMap(),
        var verificationThreshold: Float = 0.85f,
        var isVerified: Boolean = false,
        var verificationCount: Int = 0
    )

    data class SpeakerSegment(
        val speakerId: Int,
        val startTime: Double,
        val endTime: Double,
        val confidence: Float,
        val embedding: FloatArray,
        val isTransition: Boolean = false,
        val overlappingSpeakerId: Int? = null
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SpeakerSegment
            return speakerId == other.speakerId &&
                   startTime == other.startTime &&
                   endTime == other.endTime &&
                   confidence == other.confidence &&
                   embedding.contentEquals(other.embedding) &&
                   isTransition == other.isTransition &&
                   overlappingSpeakerId == other.overlappingSpeakerId
        }

        override fun hashCode(): Int {
            var result = speakerId
            result = 31 * result + startTime.hashCode()
            result = 31 * result + endTime.hashCode()
            result = 31 * result + confidence.hashCode()
            result = 31 * result + embedding.contentHashCode()
            result = 31 * result + isTransition.hashCode()
            result = 31 * result + (overlappingSpeakerId?.hashCode() ?: 0)
            return result
        }
    }

    data class VerificationResult(
        val isVerified: Boolean,
        val confidence: Float,
        val profile: SpeakerProfile?
    )

    sealed class DetectionResult {
        data class Success(
            val segments: List<SpeakerSegment>,
            val speakerCount: Int,
            val confidence: Float,
            val speakerProfiles: Map<Int, SpeakerProfile>
        ) : DetectionResult()
        data class Error(
            val code: ErrorCode,
            val message: String
        ) : DetectionResult()
    }

    enum class ErrorCode {
        MODEL_LOADING_FAILED,
        AUDIO_PROCESSING_FAILED,
        INFERENCE_FAILED,
        INVALID_INPUT
    }

    init {
        try {
            val startTime = System.currentTimeMillis()
            embeddingModel = FileUtil.loadMappedFile(context, modelName)
            interpreter = Interpreter(embeddingModel!!, Interpreter.Options().apply {
                setNumThreads(4)
                useNNAPI = true
            })
            val loadTime = System.currentTimeMillis() - startTime
            analyticsTracker.trackModelLoaded(modelName, loadTime)
            
            // Load saved profiles
            profiles.putAll(persistentProfiles.getProfiles())
        } catch (e: Exception) {
            Log.e(TAG, "Error loading model", e)
            analyticsTracker.trackModelLoadError(modelName, e.message ?: "Unknown error")
        }
    }

    fun detectSpeakers(audioFile: File): Flow<DetectionResult> = flow {
        try {
            if (!audioFile.exists()) {
                emit(DetectionResult.Error(ErrorCode.INVALID_INPUT, "Audio file not found"))
                return@flow
            }

            val startTime = System.currentTimeMillis()
            val audioLengthSeconds = audioFile.length() / (16000.0 * 2)
            analyticsTracker.trackSpeakerDetectionStarted(audioLengthSeconds)

            val segments = audioFeatureExtractor.segmentAudioWithNoiseReduction(
                audioFile,
                minSegmentDuration
            )

            val embeddings = mutableListOf<FloatArray>()
            val speakerSegments = mutableListOf<SpeakerSegment>()

            segments.forEach { (start, end, audioData) ->
                val cacheKey = generateCacheKey(audioData)
                val embedding = cache.get(cacheKey) ?: generateEmbedding(audioData)?.also {
                    cache.put(cacheKey, it)
                }
                
                if (embedding != null) {
                    embeddings.add(embedding)
                    speakerSegments.add(
                        SpeakerSegment(
                            speakerId = -1,
                            startTime = start,
                            endTime = end,
                            confidence = 0f,
                            embedding = embedding
                        )
                    )
                }
            }

            var optimalClusters: Triple<List<SpeakerSegment>, Int, Map<Int, SpeakerProfile>>? = null
            var currentThreshold = similarityThreshold

            while (currentThreshold >= minSimilarityThreshold && optimalClusters == null) {
                val clusters = clusterSpeakersWithProfiles(speakerSegments, currentThreshold)
                if (clusters.second in 2..maxSpeakers) {
                    optimalClusters = clusters
                } else {
                    currentThreshold -= adaptiveThresholdStep
                }
            }

            if (optimalClusters == null) {
                optimalClusters = clusterSpeakersWithProfiles(speakerSegments, minSimilarityThreshold)
            }

            val (labeledSegments, speakerCount, profiles) = optimalClusters
            val averageConfidence = labeledSegments.map { it.confidence }.average().toFloat()

            val segmentsWithTransitions = detectTransitionsAndOverlaps(labeledSegments)
            
            val processedSegments = postProcessSegments(segmentsWithTransitions)

            val totalDuration = System.currentTimeMillis() - startTime

            analyticsTracker.trackSpeakerDetectionCompleted(
                speakerCount = speakerCount,
                confidence = averageConfidence,
                durationMs = totalDuration,
                success = true
            )

            emit(DetectionResult.Success(
                segments = processedSegments,
                speakerCount = speakerCount,
                confidence = averageConfidence,
                speakerProfiles = profiles
            ))

        } catch (e: Exception) {
            handleError(e)
            emit(DetectionResult.Error(
                code = when (e) {
                    is IllegalArgumentException -> ErrorCode.INVALID_INPUT
                    is IllegalStateException -> ErrorCode.MODEL_LOADING_FAILED
                    else -> ErrorCode.INFERENCE_FAILED
                },
                message = "Error during speaker detection: ${e.message}"
            ))
        }
    }.flowOn(Dispatchers.Default)

    private fun detectTransitionsAndOverlaps(
        segments: List<SpeakerSegment>
    ): List<SpeakerSegment> {
        val result = mutableListOf<SpeakerSegment>()
        val overlapCandidates = mutableListOf<Pair<SpeakerSegment, SpeakerSegment>>()
        
        segments.windowed(3, 1, true) { window ->
            val prev = window.getOrNull(0)
            val current = window[1]
            val next = window.getOrNull(2)
            
            if (next != null && next.startTime - current.endTime < speakerOverlapWindow) {
                val similarity = cosineSimilarity(current.embedding, next.embedding)
                val energyRatio = calculateEnergyRatio(current.embedding, next.embedding)
                
                if (similarity > transitionThreshold && similarity < similarityThreshold) {
                    val overlapDuration = next.startTime - current.endTime
                    if (overlapDuration >= minOverlapDuration && overlapDuration <= maxOverlapGap) {
                        overlapCandidates.add(current to next)
                        
                        val transitionSegment = current.copy(
                            isTransition = true,
                            overlappingSpeakerId = next.speakerId,
                            confidence = calculateOverlapConfidence(similarity, energyRatio)
                        )
                        result.add(transitionSegment)
                        return@windowed
                    }
                }
            }
            
            if (prev != null && current.speakerId == prev.speakerId) {
                val similarity = cosineSimilarity(current.embedding, prev.embedding)
                if (similarity > maxSimilarityThreshold) {
                    val mergedSegment = current.copy(
                        startTime = prev.startTime,
                        confidence = (current.confidence + prev.confidence) / 2
                    )
                    result.removeLast()
                    result.add(mergedSegment)
                    return@windowed
                }
            }
            
            result.add(current)
        }
        
        postProcessOverlaps(result, overlapCandidates)
        
        return result
    }

    private fun calculateEnergyRatio(embedding1: FloatArray, embedding2: FloatArray): Float {
        val energy1 = embedding1.map { it * it }.sum()
        val energy2 = embedding2.map { it * it }.sum()
        return min(energy1, energy2) / max(energy1, energy2)
    }

    private fun calculateOverlapConfidence(similarity: Float, energyRatio: Float): Float {
        return (similarity * 0.7f + energyRatio * 0.3f)
    }

    private fun postProcessOverlaps(
        segments: MutableList<SpeakerSegment>,
        overlapCandidates: List<Pair<SpeakerSegment, SpeakerSegment>>
    ) {
        val overlaps = overlapCandidates.groupBy { it.first.speakerId to it.second.speakerId }
        
        overlaps.forEach { (speakers, overlaps) ->
            if (overlaps.size >= 2) {
                overlaps.forEach { (first, second) ->
                    val idx = segments.indexOfFirst { it.startTime == first.startTime }
                    if (idx != -1) {
                        segments[idx] = segments[idx].copy(
                            confidence = segments[idx].confidence * 1.2f
                        )
                    }
                }
            }
        }
    }

    private fun clusterSpeakersWithProfiles(
        segments: List<SpeakerSegment>,
        threshold: Float
    ): Triple<List<SpeakerSegment>, Int, Map<Int, SpeakerProfile>> {
        val clusters = mutableListOf<MutableList<SpeakerSegment>>()
        val profiles = mutableMapOf<Int, SpeakerProfile>()
        
        persistentProfiles.forEach { (id, profile) ->
            profiles[id] = profile
        }
        
        segments.forEach { segment ->
            var assignedCluster = false
            var bestMatch: Pair<Int, Float>? = null
            
            profiles.forEach { (id, profile) ->
                val similarity = calculateProfileSimilarity(segment.embedding, profile)
                if (similarity > threshold && (bestMatch == null || similarity > bestMatch.second)) {
                    bestMatch = id to similarity
                }
            }
            
            bestMatch?.let { (profileId, similarity) ->
                val clusterIdx = clusters.indexOfFirst { cluster ->
                    cluster.firstOrNull()?.speakerId == profileId
                }
                
                if (clusterIdx != -1) {
                    clusters[clusterIdx].add(segment.copy(
                        speakerId = profileId,
                        confidence = similarity
                    ))
                } else {
                    clusters.add(mutableListOf(segment.copy(
                        speakerId = profileId,
                        confidence = similarity
                    )))
                }
                assignedCluster = true
                
                profiles[profileId]?.let { profile ->
                    profile.embeddings.add(segment.embedding)
                    profile.totalDuration += segment.endTime - segment.startTime
                    profile.averageConfidence = (profile.averageConfidence + similarity) / 2
                    profile.lastUpdated = System.currentTimeMillis()
                }
            }
            
            if (!assignedCluster) {
                for ((index, cluster) in clusters.withIndex()) {
                    val avgSimilarity = cluster.map { existingSegment ->
                        cosineSimilarity(segment.embedding, existingSegment.embedding)
                    }.average()
                    
                    if (avgSimilarity > threshold) {
                        cluster.add(segment.copy(
                            speakerId = index + 1,
                            confidence = avgSimilarity.toFloat()
                        ))
                        assignedCluster = true
                        break
                    }
                }
            }
            
            if (!assignedCluster && clusters.size < maxSpeakers) {
                val newCluster = mutableListOf(segment.copy(
                    speakerId = clusters.size + 1,
                    confidence = 1.0f
                ))
                clusters.add(newCluster)
                
                val speakerId = clusters.size
                profiles[speakerId] = SpeakerProfile(
                    id = speakerId,
                    embeddings = mutableListOf(segment.embedding),
                    totalDuration = segment.endTime - segment.startTime,
                    averageConfidence = 1.0f,
                    speakerCharacteristics = extractVoiceCharacteristics(segment.embedding)
                )
            }
        }
        
        persistentProfiles.saveProfiles(profiles)
        
        return Triple(
            clusters.flatten(),
            clusters.size,
            profiles
        )
    }

    private fun calculateProfileSimilarity(embedding: FloatArray, profile: SpeakerProfile): Float {
        if (profile.embeddings.isEmpty()) return 0f
        
        val recentEmbeddings = profile.embeddings.takeLast(5)
        val recentSimilarity = recentEmbeddings.map { 
            cosineSimilarity(embedding, it)
        }.average().toFloat()
        
        val embeddingCharacteristics = extractVoiceCharacteristics(embedding)
        val characteristicsSimilarity = calculateCharacteristicsSimilarity(
            embeddingCharacteristics,
            profile.speakerCharacteristics
        )
        
        return recentSimilarity * 0.7f + characteristicsSimilarity * 0.3f
    }

    private fun extractVoiceCharacteristics(embedding: FloatArray): Map<String, Float> {
        val chunkSize = 32 // Reduced from 64 for finer-grained analysis
        return mapOf(
            // Pitch characteristics
            "pitch_mean" to embedding.take(chunkSize).average().toFloat(),
            "pitch_variance" to calculateVariance(embedding.take(chunkSize)),
            "pitch_range" to calculateRange(embedding.take(chunkSize)),
            
            // Energy characteristics
            "energy_mean" to embedding.drop(chunkSize).take(chunkSize).average().toFloat(),
            "energy_variance" to calculateVariance(embedding.drop(chunkSize).take(chunkSize)),
            "energy_contour" to calculateContour(embedding.drop(chunkSize).take(chunkSize)),
            
            // Tempo characteristics
            "tempo_mean" to embedding.drop(2 * chunkSize).take(chunkSize).average().toFloat(),
            "tempo_variance" to calculateVariance(embedding.drop(2 * chunkSize).take(chunkSize)),
            "tempo_regularity" to calculateRegularity(embedding.drop(2 * chunkSize).take(chunkSize)),
            
            // Timbre characteristics
            "timbre_brightness" to calculateBrightness(embedding.drop(3 * chunkSize).take(chunkSize)),
            "timbre_roughness" to calculateRoughness(embedding.drop(3 * chunkSize).take(chunkSize)),
            "timbre_warmth" to calculateWarmth(embedding.drop(3 * chunkSize).take(chunkSize)),
            
            // Spectral characteristics
            "spectral_centroid" to calculateSpectralCentroid(embedding.drop(4 * chunkSize).take(chunkSize)),
            "spectral_flux" to calculateSpectralFlux(embedding.drop(4 * chunkSize).take(chunkSize)),
            "spectral_rolloff" to calculateSpectralRolloff(embedding.drop(4 * chunkSize).take(chunkSize)),
            
            // Voice quality characteristics
            "voice_breathiness" to calculateBreathiness(embedding.drop(5 * chunkSize).take(chunkSize)),
            "voice_creakiness" to calculateCreakiness(embedding.drop(5 * chunkSize).take(chunkSize)),
            "voice_nasality" to calculateNasality(embedding.drop(5 * chunkSize).take(chunkSize)),
            
            // Prosodic characteristics
            "prosody_rhythm" to calculateRhythm(embedding.drop(6 * chunkSize).take(chunkSize)),
            "prosody_stress" to calculateStress(embedding.drop(6 * chunkSize).take(chunkSize)),
            "prosody_intonation" to calculateIntonation(embedding.drop(6 * chunkSize).take(chunkSize))
        )
    }

    private fun calculateVariance(values: List<Float>): Float {
        val mean = values.average()
        return values.map { (it - mean) * (it - mean) }.average().toFloat()
    }

    private fun calculateRange(values: List<Float>): Float {
        return values.maxOrNull()?.minus(values.minOrNull() ?: 0f) ?: 0f
    }

    private fun calculateContour(values: List<Float>): Float {
        return values.windowed(2, 1).map { (a, b) -> abs(b - a) }.average().toFloat()
    }

    private fun calculateRegularity(values: List<Float>): Float {
        val diffs = values.windowed(2, 1).map { (a, b) -> abs(b - a) }
        return 1f - (calculateVariance(diffs) / values.average().toFloat())
    }

    private fun calculateBrightness(values: List<Float>): Float {
        return values.mapIndexed { index, value -> 
            value * (index + 1) 
        }.average().toFloat()
    }

    private fun calculateRoughness(values: List<Float>): Float {
        return values.windowed(2, 1).map { (a, b) -> 
            abs(a * b)
        }.average().toFloat()
    }

    private fun calculateWarmth(values: List<Float>): Float {
        val lowFreq = values.take(values.size / 2).average()
        val highFreq = values.drop(values.size / 2).average()
        return (lowFreq / (highFreq + 0.0001)).toFloat()
    }

    private fun calculateSpectralCentroid(values: List<Float>): Float {
        var weightedSum = 0f
        var sum = 0f
        values.forEachIndexed { index, value ->
            weightedSum += value * index
            sum += value
        }
        return if (sum != 0f) weightedSum / sum else 0f
    }

    private fun calculateSpectralFlux(values: List<Float>): Float {
        return values.windowed(2, 1).map { (a, b) ->
            (b - a).pow(2)
        }.average().toFloat()
    }

    private fun calculateSpectralRolloff(values: List<Float>): Float {
        val threshold = values.sum() * 0.85f
        var cumSum = 0f
        values.forEachIndexed { index, value ->
            cumSum += value
            if (cumSum >= threshold) {
                return index.toFloat() / values.size
            }
        }
        return 1f
    }

    private fun calculateBreathiness(values: List<Float>): Float {
        val harmonicContent = values.take(values.size / 2).average()
        val noiseContent = values.drop(values.size / 2).average()
        return (noiseContent / (harmonicContent + 0.0001)).toFloat()
    }

    private fun calculateCreakiness(values: List<Float>): Float {
        return values.windowed(3, 1).map { window ->
            val irregularity = abs(window[1] - (window[0] + window[2]) / 2)
            irregularity
        }.average().toFloat()
    }

    private fun calculateNasality(values: List<Float>): Float {
        val formants = values.chunked(4)
        return formants.map { formant ->
            formant.getOrNull(1)?.div(formant.getOrNull(0)?.plus(0.0001f) ?: 1f) ?: 0f
        }.average().toFloat()
    }

    private fun calculateRhythm(values: List<Float>): Float {
        val peaks = values.windowed(3, 1).mapIndexedNotNull { index, window ->
            if (window[1] > window[0] && window[1] > window[2]) index + 1 else null
        }
        return if (peaks.size >= 2) {
            val intervals = peaks.windowed(2, 1).map { (a, b) -> b - a }
            1f - calculateVariance(intervals.map { it.toFloat() })
        } else 0f
    }

    private fun calculateStress(values: List<Float>): Float {
        val mean = values.average()
        return values.count { it > mean * 1.2 }.toFloat() / values.size
    }

    private fun calculateIntonation(values: List<Float>): Float {
        return values.windowed(4, 1).map { window ->
            val slope = (window.last() - window.first()) / 3
            abs(slope)
        }.average().toFloat()
    }

    private fun calculateCharacteristicsSimilarity(
        chars1: Map<String, Float>,
        chars2: Map<String, Float>
    ): Float {
        if (chars1.isEmpty() || chars2.isEmpty()) return 0f
        
        // Weights for different characteristic groups
        val weights = mapOf(
            "pitch" to 0.15f,
            "energy" to 0.15f,
            "tempo" to 0.1f,
            "timbre" to 0.15f,
            "spectral" to 0.15f,
            "voice" to 0.15f,
            "prosody" to 0.15f
        )
        
        var weightedSimilarity = 0f
        var totalWeight = 0f
        
        chars1.forEach { (key, value1) ->
            chars2[key]?.let { value2 ->
                val weight = weights.entries.find { key.startsWith(it.key) }?.value ?: 0.1f
                weightedSimilarity += (1 - abs(value1 - value2)) * weight
                totalWeight += weight
            }
        }
        
        return if (totalWeight > 0f) weightedSimilarity / totalWeight else 0f
    }

    private fun postProcessSegments(segments: List<SpeakerSegment>): List<SpeakerSegment> {
        val mergedSegments = mutableListOf<SpeakerSegment>()
        var currentSegment = segments.firstOrNull() ?: return emptyList()

        segments.drop(1).forEach { segment ->
            if (segment.speakerId == currentSegment.speakerId &&
                segment.startTime - currentSegment.endTime < 0.5) {
                currentSegment = currentSegment.copy(
                    endTime = segment.endTime,
                    confidence = (currentSegment.confidence + segment.confidence) / 2
                )
            } else {
                if (currentSegment.endTime - currentSegment.startTime >= minSegmentDuration) {
                    mergedSegments.add(currentSegment)
                }
                currentSegment = segment
            }
        }
        mergedSegments.add(currentSegment)

        return mergedSegments
    }

    private fun generateCacheKey(audioData: FloatArray): String {
        return audioData.take(1000).joinToString(",") { it.toString() }
    }

    private fun generateEmbedding(audioData: FloatArray): FloatArray? {
        return try {
            val startTime = System.currentTimeMillis()
            val inputBuffer = ByteBuffer.allocateDirect(audioData.size * 4)
            audioData.forEach { inputBuffer.putFloat(it) }
            inputBuffer.rewind()

            val outputBuffer = ByteBuffer.allocateDirect(embeddingDimension * 4)
            interpreter?.run(inputBuffer, outputBuffer)

            outputBuffer.rewind()
            val embedding = FloatArray(embeddingDimension) { outputBuffer.getFloat() }

            analyticsTracker.trackPerformanceMetric(
                metricName = "embedding_generation",
                durationMs = System.currentTimeMillis() - startTime,
                success = true,
                additionalData = mapOf(
                    "input_size" to audioData.size,
                    "embedding_size" to embeddingDimension
                )
            )

            embedding
        } catch (e: Exception) {
            Log.e(TAG, "Error generating embedding", e)
            analyticsTracker.trackPerformanceMetric(
                metricName = "embedding_generation",
                durationMs = 0,
                success = false,
                additionalData = mapOf(
                    "error_message" to (e.message ?: "Unknown error")
                )
            )
            null
        }
    }

    private fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
        var dotProduct = 0f
        var normA = 0f
        var normB = 0f
        
        for (i in a.indices) {
            dotProduct += a[i] * b[i]
            normA += a[i] * a[i]
            normB += b[i] * b[i]
        }
        
        return dotProduct / (sqrt(normA) * sqrt(normB))
    }

    private fun handleError(e: Exception, audioFile: File? = null) {
        Log.e(TAG, "Error in speaker detection", e)
        val errorCode = when (e) {
            is IllegalArgumentException -> ErrorCode.INVALID_INPUT
            is IllegalStateException -> ErrorCode.MODEL_LOADING_FAILED
            else -> ErrorCode.INFERENCE_FAILED
        }

        analyticsTracker.trackSpeakerDetectionError(
            errorCode = errorCode.name,
            errorMessage = e.message ?: "Unknown error",
            audioLengthSeconds = audioFile?.length()?.div(16000.0 * 2) ?: 0.0
        )
    }

    fun verifySpeaker(audioData: FloatArray, speakerId: Int): Flow<VerificationResult> = flow {
        try {
            val embedding = generateEmbedding(audioData) ?: throw IllegalStateException("Failed to generate embedding")
            val profile = profiles[speakerId] ?: throw IllegalArgumentException("Unknown speaker ID")
            
            val similarity = calculateProfileSimilarity(embedding, profile)
            val isVerified = similarity >= profile.verificationThreshold
            
            if (isVerified) {
                profile.verificationCount++
                // Adapt verification threshold based on successful verifications
                if (profile.verificationCount >= 5) {
                    profile.verificationThreshold = (profile.verificationThreshold * 0.9f + similarity * 0.1f)
                        .coerceIn(0.75f, 0.95f)
                }
                
                // Update profile with new embedding
                profile.embeddings.add(embedding)
                if (profile.embeddings.size > 20) {  // Keep only recent embeddings
                    profile.embeddings.removeAt(0)
                }
                profile.lastUpdated = System.currentTimeMillis()
                
                // Update characteristics
                val newCharacteristics = extractVoiceCharacteristics(embedding)
                profile.speakerCharacteristics = updateCharacteristics(
                    profile.speakerCharacteristics,
                    newCharacteristics
                )
                
                persistentProfiles.saveProfiles(profiles)
            }
            
            emit(VerificationResult(
                isVerified = isVerified,
                confidence = similarity,
                profile = profile
            ))
            
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying speaker", e)
            emit(VerificationResult(
                isVerified = false,
                confidence = 0f,
                profile = null
            ))
        }
    }.flowOn(Dispatchers.Default)

    fun registerKnownSpeaker(
        name: String,
        audioData: FloatArray,
        initialThreshold: Float = 0.85f
    ): Flow<SpeakerProfile> = flow {
        try {
            val embedding = generateEmbedding(audioData) ?: throw IllegalStateException("Failed to generate embedding")
            val characteristics = extractVoiceCharacteristics(embedding)
            
            val newId = (profiles.keys.maxOrNull() ?: 0) + 1
            val profile = SpeakerProfile(
                id = newId,
                name = name,
                embeddings = mutableListOf(embedding),
                totalDuration = 5.0,  // Initial sample duration
                averageConfidence = 1.0f,
                speakerCharacteristics = characteristics,
                verificationThreshold = initialThreshold,
                isVerified = true,
                verificationCount = 1
            )
            
            profiles[newId] = profile
            persistentProfiles.saveProfiles(profiles)
            
            emit(profile)
            
        } catch (e: Exception) {
            Log.e(TAG, "Error registering known speaker", e)
            throw e
        }
    }.flowOn(Dispatchers.Default)

    private fun updateCharacteristics(
        oldCharacteristics: Map<String, Float>,
        newCharacteristics: Map<String, Float>,
        learningRate: Float = 0.1f
    ): Map<String, Float> {
        return oldCharacteristics.mapValues { (key, oldValue) ->
            newCharacteristics[key]?.let { newValue ->
                oldValue * (1 - learningRate) + newValue * learningRate
            } ?: oldValue
        }
    }

    fun startRealtimeDetection() {
        lastProcessedTime = System.currentTimeMillis()
        realtimeBuffer.clear()
        realtimeSegments.clear()
        _realtimeSpeakers.value = emptyList()
    }

    fun processRealtimeAudio(audioData: FloatArray, timestamp: Long) {
        // Add new audio data to buffer
        audioData.forEach { sample ->
            realtimeBuffer.add(sample)
        }

        // Check if it's time to process
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastProcessedTime >= processingInterval && realtimeBuffer.size >= 16000) {
            processRealtimeBuffer(timestamp)
            lastProcessedTime = currentTime
        }
    }

    private fun processRealtimeBuffer(timestamp: Long) {
        val bufferData = realtimeBuffer.toFloatArray()
        
        scope.launch(Dispatchers.Default) {
            try {
                val embedding = generateEmbedding(bufferData)
                if (embedding != null) {
                    val characteristics = extractVoiceCharacteristics(embedding)
                    
                    // Find best matching profile
                    var bestMatch: Pair<SpeakerProfile, Float>? = null
                    profiles.values.forEach { profile ->
                        val similarity = calculateProfileSimilarity(embedding, profile)
                        if (similarity >= realtimeThreshold && 
                            (bestMatch == null || similarity > bestMatch.second)) {
                            bestMatch = profile to similarity
                        }
                    }
                    
                    val segment = SpeakerSegment(
                        speakerId = bestMatch?.first?.id ?: -1,
                        startTime = timestamp / 1000.0,
                        endTime = (timestamp + processingInterval) / 1000.0,
                        confidence = bestMatch?.second ?: 0f,
                        embedding = embedding
                    )
                    
                    // Update realtime segments
                    realtimeSegments.add(segment)
                    if (realtimeSegments.size > 20) {  // Keep last 10 seconds
                        realtimeSegments.removeAt(0)
                    }
                    
                    // Post-process segments for smoother speaker transitions
                    val processedSegments = postProcessRealtimeSegments(realtimeSegments)
                    _realtimeSpeakers.value = processedSegments
                    
                    // Update profiles of verified speakers
                    bestMatch?.let { (profile, similarity) ->
                        if (profile.isVerified && similarity >= profile.verificationThreshold) {
                            updateProfileWithRealtimeData(profile, embedding, characteristics)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing realtime audio", e)
            }
        }
    }

    private fun postProcessRealtimeSegments(
        segments: List<SpeakerSegment>
    ): List<SpeakerSegment> {
        if (segments.size < 2) return segments
        
        val processed = mutableListOf<SpeakerSegment>()
        var currentSegment = segments.first()
        
        segments.drop(1).forEach { segment ->
            if (segment.speakerId == currentSegment.speakerId) {
                // Merge segments of same speaker
                currentSegment = currentSegment.copy(
                    endTime = segment.endTime,
                    confidence = (currentSegment.confidence + segment.confidence) / 2
                )
            } else {
                // Check if speaker change is stable
                val futureSegments = segments.dropWhile { it.startTime <= segment.endTime }
                val isSpeakerChangeStable = futureSegments
                    .take(2)
                    .all { it.speakerId == segment.speakerId }
                
                if (isSpeakerChangeStable) {
                    processed.add(currentSegment)
                    currentSegment = segment
                } else {
                    // Keep previous speaker if change is not stable
                    currentSegment = currentSegment.copy(
                        endTime = segment.endTime
                    )
                }
            }
        }
        
        processed.add(currentSegment)
        return processed
    }

    private fun updateProfileWithRealtimeData(
        profile: SpeakerProfile,
        embedding: FloatArray,
        characteristics: Map<String, Float>
    ) {
        profile.embeddings.add(embedding)
        if (profile.embeddings.size > 20) {
            profile.embeddings.removeAt(0)
        }
        
        profile.speakerCharacteristics = updateCharacteristics(
            profile.speakerCharacteristics,
            characteristics,
            0.05f  // Lower learning rate for realtime updates
        )
        
        profile.lastUpdated = System.currentTimeMillis()
        persistentProfiles.saveProfiles(profiles)
    }

    private class RingBuffer<T>(private val capacity: Int) {
        private val buffer = ArrayList<T>(capacity)
        
        val size: Int get() = buffer.size
        
        fun add(element: T) {
            if (buffer.size >= capacity) {
                buffer.removeAt(0)
            }
            buffer.add(element)
        }
        
        fun clear() {
            buffer.clear()
        }
        
        fun toFloatArray(): FloatArray {
            return (buffer as ArrayList<Float>).toFloatArray()
        }
    }

    companion object {
        private const val TAG = "MLSpeakerDetectionService"
    }
} 