package com.example.clicknote.service

import android.content.Context
import android.content.SharedPreferences
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.clicknote.analytics.AnalyticsTracker
import com.example.clicknote.domain.model.SpeakerProfile
import com.example.clicknote.domain.service.SpeakerDetectionService
import com.example.clicknote.util.AudioFeatureExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.*

@Singleton
class MLSpeakerDetectionService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val audioFeatureExtractor: AudioFeatureExtractor,
    private val analyticsTracker: AnalyticsTracker,
    private val sharedPreferences: SharedPreferences
) : SpeakerDetectionService {

    override val id: String = "ml_speaker_detection"
    private var initialized = false
    private val embeddingDimension = 256
    private val minSegmentDuration = 1.0
    private val maxSpeakers = 10
    private var similarityThreshold = 0.82f
    private val minSimilarityThreshold = 0.70f
    private val maxSimilarityThreshold = 0.95f

    private val cache = LruCache<String, FloatArray>(200)
    private val profiles = mutableMapOf<Int, SpeakerProfile>()
    private val _speakerConfidence = MutableStateFlow(0f)
    val speakerConfidence: StateFlow<Float> = _speakerConfidence.asStateFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override suspend fun detectSpeakers(audioData: ByteArray): Result<Int> = runCatching {
        withContext(Dispatchers.Default) {
            val features = audioFeatureExtractor.extractFeatures(audioData)
            analyticsTracker.trackPerformanceMetric(
                metricName = "speaker_detection",
                durationMs = System.currentTimeMillis(),
                success = true,
                additionalData = mapOf("feature_count" to features.size)
            )
            features.size
        }
    }

    override suspend fun identifySpeakers(audioData: ByteArray): Result<Map<String, String>> = runCatching {
        withContext(Dispatchers.Default) {
            val features = audioFeatureExtractor.extractFeatures(audioData)
            analyticsTracker.trackPerformanceMetric(
                metricName = "speaker_identification",
                durationMs = System.currentTimeMillis(),
                success = true,
                additionalData = mapOf("feature_count" to features.size)
            )
            features.mapIndexed { index, _ -> 
                "Speaker${index + 1}" to "Unknown"
            }.toMap()
        }
    }

    override suspend fun detectSpeakers(file: File): Result<List<String>> = runCatching {
        withContext(Dispatchers.Default) {
            val audioData = file.readBytes()
            val features = audioFeatureExtractor.extractFeatures(audioData)
            analyticsTracker.trackPerformanceMetric(
                metricName = "file_speaker_detection",
                durationMs = System.currentTimeMillis(),
                success = true,
                additionalData = mapOf("feature_count" to features.size)
            )
            features.mapIndexed { index, _ -> "Speaker${index + 1}" }
        }
    }

    override suspend fun trainSpeakerProfile(
        profile: SpeakerProfile,
        audioData: ByteArray
    ): Result<SpeakerProfile> = runCatching {
        profile
    }

    override suspend fun deleteSpeakerProfile(speakerId: String): Result<Unit> = runCatching {
        profiles.remove(speakerId.toIntOrNull())
    }

    override suspend fun getSpeakerConfidence(
        speakerId: String,
        audioSample: ByteArray
    ): Result<Float> = runCatching {
        withContext(Dispatchers.Default) {
            val features = audioFeatureExtractor.extractFeatures(audioSample)
            features.maxOfOrNull { it } ?: 0f
        }
    }

    override suspend fun getKnownSpeakers(): Result<List<SpeakerProfile>> = runCatching {
        profiles.values.toList()
    }

    override fun getSpeakerConfidence(): Flow<Float> = speakerConfidence

    override fun isInitialized(): Boolean = initialized

    override suspend fun cleanup() {
        initialized = false
        profiles.clear()
        cache.evictAll()
        scope.launch {
            analyticsTracker.trackPerformanceMetric(
                metricName = "service_cleanup",
                durationMs = System.currentTimeMillis(),
                success = true
            )
        }
    }

    private fun calculateSimilarity(v1: FloatArray, v2: FloatArray): Float {
        if (v1.isEmpty() || v2.isEmpty() || v1.size != v2.size) {
            return 0f
        }

        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f

        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            norm1 += v1[i] * v1[i]
            norm2 += v2[i] * v2[i]
        }

        if (norm1 <= 0 || norm2 <= 0) {
            return 0f
        }

        return dotProduct / (sqrt(norm1) * sqrt(norm2))
    }

    data class SpeakerSegment(
        val speakerId: Int,
        val startTime: Double,
        val endTime: Double,
        val confidence: Float,
        val embedding: FloatArray
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false

            other as SpeakerSegment
            return speakerId == other.speakerId &&
                   startTime == other.startTime &&
                   endTime == other.endTime &&
                   confidence == other.confidence &&
                   embedding.contentEquals(other.embedding)
        }

        override fun hashCode(): Int {
            var result = speakerId
            result = 31 * result + startTime.hashCode()
            result = 31 * result + endTime.hashCode()
            result = 31 * result + confidence.hashCode()
            result = 31 * result + embedding.contentHashCode()
            return result
        }
    }

    companion object {
        private const val TAG = "MLSpeakerDetection"
        private const val SAMPLE_RATE = 16000
        private const val RECORDING_DURATION_MS = 5000
        private const val BUFFER_SIZE = SAMPLE_RATE * (RECORDING_DURATION_MS / 1000)
    }
}