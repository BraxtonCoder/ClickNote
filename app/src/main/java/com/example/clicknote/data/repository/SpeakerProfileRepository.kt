package com.example.clicknote.data.repository

import com.example.clicknote.analytics.AnalyticsTracker
import com.example.clicknote.data.dao.SpeakerProfileDao
import com.example.clicknote.data.entity.SpeakerProfile
import com.example.clicknote.service.MLSpeakerDetectionService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeakerProfileRepository @Inject constructor(
    private val speakerProfileDao: SpeakerProfileDao,
    private val speakerDetectionService: MLSpeakerDetectionService,
    private val analyticsTracker: AnalyticsTracker
) {
    fun getAllActiveProfiles() = speakerProfileDao.getAllActiveProfiles()

    fun getRecentProfiles(since: LocalDateTime) = speakerProfileDao.getRecentProfiles(since)

    suspend fun createProfile(name: String, initialEmbeddings: List<FloatArray>): Result<Long> = 
    withContext(Dispatchers.IO) {
        try {
            val profile = SpeakerProfile(
                name = name,
                embeddings = initialEmbeddings,
                sampleCount = initialEmbeddings.size,
                averageConfidence = 1.0f // Initial confidence for manually created profile
            )
            
            val profileId = speakerProfileDao.insert(profile)
            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_creation",
                durationMs = 0,
                success = true,
                additionalData = mapOf(
                    "embedding_count" to initialEmbeddings.size,
                    "profile_id" to profileId
                )
            )
            
            Result.success(profileId)
        } catch (e: Exception) {
            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_creation",
                durationMs = 0,
                success = false,
                additionalData = mapOf(
                    "error_message" to (e.message ?: "Unknown error")
                )
            )
            Result.failure(e)
        }
    }

    suspend fun updateProfile(
        profileId: Long,
        newEmbeddings: List<FloatArray>,
        confidence: Float
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = speakerProfileDao.getProfileById(profileId)
            if (profile != null) {
                val updatedEmbeddings = profile.embeddings + newEmbeddings
                val newSampleCount = profile.sampleCount + newEmbeddings.size
                val newConfidence = (profile.averageConfidence * profile.sampleCount + 
                                   confidence * newEmbeddings.size) / newSampleCount

                speakerProfileDao.updateProfileEmbeddings(
                    profileId = profileId,
                    newEmbeddings = updatedEmbeddings,
                    newSampleCount = newSampleCount,
                    newConfidence = newConfidence
                )

                analyticsTracker.trackPerformanceMetric(
                    metricName = "profile_update",
                    durationMs = 0,
                    success = true,
                    additionalData = mapOf(
                        "profile_id" to profileId,
                        "new_sample_count" to newSampleCount,
                        "new_confidence" to newConfidence
                    )
                )
                
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Profile not found"))
            }
        } catch (e: Exception) {
            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_update",
                durationMs = 0,
                success = false,
                additionalData = mapOf(
                    "profile_id" to profileId,
                    "error_message" to (e.message ?: "Unknown error")
                )
            )
            Result.failure(e)
        }
    }

    suspend fun findMatchingProfile(
        embedding: FloatArray,
        minConfidence: Float = 0.85f
    ): Result<SpeakerProfile?> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val trainedProfiles = speakerProfileDao.getTrainedProfiles()
            
            val bestMatch = trainedProfiles.maxByOrNull { profile ->
                profile.embeddings.maxOf { profileEmbedding ->
                    speakerDetectionService.cosineSimilarity(embedding, profileEmbedding)
                }
            }

            val confidence = bestMatch?.let { profile ->
                profile.embeddings.maxOf { profileEmbedding ->
                    speakerDetectionService.cosineSimilarity(embedding, profileEmbedding)
                }
            } ?: 0f

            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_matching",
                durationMs = System.currentTimeMillis() - startTime,
                success = true,
                additionalData = mapOf(
                    "matched_profile" to (bestMatch?.id ?: -1),
                    "confidence" to confidence,
                    "profiles_checked" to trainedProfiles.size
                )
            )

            Result.success(if (confidence >= minConfidence) bestMatch else null)
        } catch (e: Exception) {
            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_matching",
                durationMs = 0,
                success = false,
                additionalData = mapOf(
                    "error_message" to (e.message ?: "Unknown error")
                )
            )
            Result.failure(e)
        }
    }

    suspend fun deactivateProfile(profileId: Long) = withContext(Dispatchers.IO) {
        try {
            speakerProfileDao.deactivateProfile(profileId)
            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_deactivation",
                durationMs = 0,
                success = true,
                additionalData = mapOf("profile_id" to profileId)
            )
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_deactivation",
                durationMs = 0,
                success = false,
                additionalData = mapOf(
                    "profile_id" to profileId,
                    "error_message" to (e.message ?: "Unknown error")
                )
            )
            Result.failure(e)
        }
    }

    suspend fun mergeProfiles(sourceId: Long, targetId: Long) = withContext(Dispatchers.IO) {
        try {
            speakerProfileDao.mergeProfiles(sourceId, targetId)
            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_merge",
                durationMs = 0,
                success = true,
                additionalData = mapOf(
                    "source_id" to sourceId,
                    "target_id" to targetId
                )
            )
            Result.success(Unit)
        } catch (e: Exception) {
            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_merge",
                durationMs = 0,
                success = false,
                additionalData = mapOf(
                    "source_id" to sourceId,
                    "target_id" to targetId,
                    "error_message" to (e.message ?: "Unknown error")
                )
            )
            Result.failure(e)
        }
    }
} 