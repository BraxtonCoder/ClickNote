package com.example.clicknote.data.repository

import android.content.Context
import com.example.clicknote.analytics.AnalyticsTracker
import com.example.clicknote.data.dao.SpeakerProfileDao
import com.example.clicknote.data.entity.SpeakerProfileEntity
import com.example.clicknote.data.SharedPreferencesManager
import com.example.clicknote.domain.model.SpeakerProfile
import com.example.clicknote.domain.repository.SpeakerProfileRepository
import com.example.clicknote.util.AudioFeatureExtractor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.sqrt

@Singleton
class SpeakerProfileRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: SharedPreferencesManager,
    private val audioFeatureExtractor: AudioFeatureExtractor,
    private val speakerProfileDao: SpeakerProfileDao,
    private val analyticsTracker: AnalyticsTracker
) : SpeakerProfileRepository {

    private val _profiles = MutableStateFlow<List<SpeakerProfile>>(emptyList())
    override val profiles: StateFlow<List<SpeakerProfile>> = _profiles.asStateFlow()

    init {
        _profiles.value = preferencesManager.getAllSpeakerProfiles()
    }

    override suspend fun createProfile(name: String): Result<SpeakerProfile> = runCatching {
        val profile = SpeakerProfile.createEmpty(
            id = generateProfileId(),
            name = name
        )
        saveProfile(profile)
        profile
    }

    override suspend fun getProfile(id: String): Result<SpeakerProfile> = runCatching {
        preferencesManager.getSpeakerProfile(id) ?: throw IllegalArgumentException("Profile not found")
    }

    override suspend fun updateProfile(profile: SpeakerProfile): Result<Unit> = runCatching {
        saveProfile(profile)
    }

    override suspend fun deleteProfile(id: String): Result<Unit> = runCatching {
        preferencesManager.deleteSpeakerProfile(id)
        refreshProfiles()
    }

    override suspend fun getAllProfiles(): Result<List<SpeakerProfile>> = runCatching {
        preferencesManager.getAllSpeakerProfiles()
    }

    override suspend fun matchSpeaker(audioData: ByteArray): Result<SpeakerProfile?> = runCatching {
        withContext(Dispatchers.Default) {
            val features = audioFeatureExtractor.extractFeatures(audioData)
            val profiles = preferencesManager.getAllSpeakerProfiles()

            profiles.maxByOrNull { profile ->
                calculateSimilarity(
                    features,
                    profile.speakerCharacteristics.values.toFloatArray()
                )
            }?.takeIf { profile ->
                val similarity = calculateSimilarity(
                    features,
                    profile.speakerCharacteristics.values.toFloatArray()
                )
                similarity >= profile.verificationThreshold
            }
        }
    }

    override suspend fun verifyProfile(
        profile: SpeakerProfile,
        audioData: ByteArray
    ): Result<Boolean> = runCatching {
        withContext(Dispatchers.Default) {
            val features = audioFeatureExtractor.extractFeatures(audioData)
            val similarity = calculateSimilarity(
                features,
                profile.speakerCharacteristics.values.toFloatArray()
            )
            similarity >= profile.verificationThreshold
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

    private fun generateProfileId(): String {
        return "profile_${System.currentTimeMillis()}_${(0..9999).random()}"
    }

    private suspend fun saveProfile(profile: SpeakerProfile) {
        val entity = SpeakerProfileEntity.fromDomain(profile)
        speakerProfileDao.insert(entity)
        refreshProfiles()
    }

    private suspend fun refreshProfiles() {
        _profiles.value = speakerProfileDao.getAllProfiles()
            .map { it.toDomain() }
    }

    fun getAllActiveProfiles(): Flow<List<SpeakerProfile>> = 
        speakerProfileDao.getActiveSpeakers()
            .map { entities -> entities.map { it.toDomain() } }

    fun getRecentProfiles(query: String): Flow<List<SpeakerProfile>> = 
        speakerProfileDao.searchSpeakers(query)
            .map { entities -> entities.map { it.toDomain() } }

    suspend fun createProfile(name: String, features: FloatArray): Result<Unit> = 
    withContext(Dispatchers.IO) {
        try {
            val profile = SpeakerProfile.createEmpty(
                id = generateProfileId(),
                name = name
            ).copy(
                speakerCharacteristics = mapOf("features" to features.average().toFloat())
            )
            
            saveProfile(profile)
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
            val profile = speakerProfileDao.getSpeakerById(profileId)?.toDomain()
            if (profile != null) {
                val updatedProfile = profile.copy(averageConfidence = newConfidence)
                saveProfile(updatedProfile)

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
        features: FloatArray,
        minConfidence: Float = 0.85f
    ): Result<SpeakerProfile?> = withContext(Dispatchers.IO) {
        try {
            val startTime = System.currentTimeMillis()
            val trainedProfiles = speakerProfileDao.getActiveSpeakers()
                .first()
                .map { it.toDomain() }
            
            val bestMatch = trainedProfiles.maxByOrNull { profile ->
                calculateSimilarity(features, profile.speakerCharacteristics.values.toFloatArray())
            }

            val confidence = bestMatch?.let { profile ->
                calculateSimilarity(features, profile.speakerCharacteristics.values.toFloatArray())
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

    companion object {
        private const val TAG = "SpeakerProfileRepo"
        private const val MIN_SIMILARITY_THRESHOLD = 0.70f
        private const val DEFAULT_VERIFICATION_THRESHOLD = 0.85f
    }
} 