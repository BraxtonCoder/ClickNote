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
    fun getAllActiveProfiles(): Flow<List<SpeakerProfile>> = 
        speakerProfileDao.getActiveSpeakers()

    fun getRecentProfiles(query: String): Flow<List<SpeakerProfile>> = 
        speakerProfileDao.searchSpeakers(query)

    suspend fun createProfile(name: String, voiceSignature: String): Result<Unit> = 
    withContext(Dispatchers.IO) {
        try {
            val profile = SpeakerProfile(
                name = name,
                voiceSignature = voiceSignature
            )
            
            speakerProfileDao.insert(profile)
            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_creation",
                durationMs = 0,
                success = true,
                additionalData = mapOf(
                    "profile_id" to profile.id
                )
            )
            
            Result.success(Unit)
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
        profileId: String,
        newConfidence: Float
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val profile = speakerProfileDao.getSpeakerById(profileId)
            if (profile != null) {
                speakerProfileDao.updateConfidence(
                    id = profileId,
                    newConfidence = newConfidence
                )

                analyticsTracker.trackPerformanceMetric(
                    metricName = "profile_update",
                    durationMs = 0,
                    success = true,
                    additionalData = mapOf(
                        "profile_id" to profileId,
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
        voiceSignature: String,
        minConfidence: Float = 0.85f
    ): Result<SpeakerProfile?> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val trainedProfiles = speakerProfileDao.getActiveSpeakers().first()
            
            val bestMatch = trainedProfiles.maxByOrNull { profile ->
                speakerDetectionService.compareVoiceSignatures(voiceSignature, profile.voiceSignature)
            }

            val confidence = bestMatch?.let { profile ->
                speakerDetectionService.compareVoiceSignatures(voiceSignature, profile.voiceSignature)
            } ?: 0f

            analyticsTracker.trackPerformanceMetric(
                metricName = "profile_matching",
                durationMs = System.currentTimeMillis() - startTime,
                success = true,
                additionalData = mapOf(
                    "matched_profile" to (bestMatch?.id ?: "none"),
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

    suspend fun deactivateProfile(profileId: String) = withContext(Dispatchers.IO) {
        try {
            speakerProfileDao.deactivate(profileId)
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

    suspend fun updateLastUsed(profileId: String) = withContext(Dispatchers.IO) {
        try {
            speakerProfileDao.updateLastUsed(profileId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 