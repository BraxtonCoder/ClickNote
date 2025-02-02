package com.example.clicknote.data

import android.content.Context
import android.content.SharedPreferences
import android.util.Base64
import com.example.clicknote.service.MLSpeakerDetectionService.SpeakerProfile
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedPreferences @Inject constructor(
    @ApplicationContext context: Context
) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()
    private var cachedProfiles: Map<Int, SpeakerProfile>? = null

    fun getProfiles(): Map<Int, SpeakerProfile> {
        cachedProfiles?.let { return it }

        val json = prefs.getString(KEY_SPEAKER_PROFILES, null) ?: return emptyMap()
        val type = object : TypeToken<Map<Int, SerializableSpeakerProfile>>() {}.type
        
        return try {
            gson.fromJson<Map<Int, SerializableSpeakerProfile>>(json, type)
                .mapValues { (_, serializable) -> serializable.toSpeakerProfile() }
                .also { cachedProfiles = it }
        } catch (e: Exception) {
            emptyMap()
        }
    }

    fun saveProfiles(profiles: Map<Int, SpeakerProfile>) {
        val serializableProfiles = profiles.mapValues { (_, profile) ->
            SerializableSpeakerProfile.fromSpeakerProfile(profile)
        }
        
        prefs.edit()
            .putString(KEY_SPEAKER_PROFILES, gson.toJson(serializableProfiles))
            .apply()
        
        cachedProfiles = profiles
    }

    private data class SerializableSpeakerProfile(
        val id: Int,
        val name: String?,
        val encodedEmbeddings: List<String>,
        val totalDuration: Double,
        val averageConfidence: Float,
        val lastUpdated: Long,
        val speakerCharacteristics: Map<String, Float>,
        val verificationThreshold: Float,
        val isVerified: Boolean,
        val verificationCount: Int
    ) {
        fun toSpeakerProfile(): SpeakerProfile {
            val embeddings = encodedEmbeddings.map { encoded ->
                val bytes = Base64.decode(encoded, Base64.DEFAULT)
                FloatArray(bytes.size / 4) { i ->
                    java.nio.ByteBuffer.wrap(bytes, i * 4, 4).float
                }
            }
            
            return SpeakerProfile(
                id = id,
                name = name,
                embeddings = embeddings.toMutableList(),
                totalDuration = totalDuration,
                averageConfidence = averageConfidence,
                lastUpdated = lastUpdated,
                speakerCharacteristics = speakerCharacteristics,
                verificationThreshold = verificationThreshold,
                isVerified = isVerified,
                verificationCount = verificationCount
            )
        }

        companion object {
            fun fromSpeakerProfile(profile: SpeakerProfile): SerializableSpeakerProfile {
                val encodedEmbeddings = profile.embeddings.map { embedding ->
                    val bytes = ByteArray(embedding.size * 4)
                    embedding.forEachIndexed { i, value ->
                        System.arraycopy(
                            java.nio.ByteBuffer.allocate(4).putFloat(value).array(),
                            0,
                            bytes,
                            i * 4,
                            4
                        )
                    }
                    Base64.encodeToString(bytes, Base64.DEFAULT)
                }
                
                return SerializableSpeakerProfile(
                    id = profile.id,
                    name = profile.name,
                    encodedEmbeddings = encodedEmbeddings,
                    totalDuration = profile.totalDuration,
                    averageConfidence = profile.averageConfidence,
                    lastUpdated = profile.lastUpdated,
                    speakerCharacteristics = profile.speakerCharacteristics,
                    verificationThreshold = profile.verificationThreshold,
                    isVerified = profile.isVerified,
                    verificationCount = profile.verificationCount
                )
            }
        }
    }

    companion object {
        private const val PREFS_NAME = "clicknote_prefs"
        private const val KEY_SPEAKER_PROFILES = "speaker_profiles"
    }
} 