package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.data.dao.SpeakerProfileDao
import com.example.clicknote.domain.model.SpeakerProfile
import com.example.clicknote.service.SpeakerProfileService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SpeakerProfileServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val speakerProfileDao: SpeakerProfileDao
) : SpeakerProfileService {

    override fun getAllProfiles(): Flow<List<SpeakerProfile>> {
        return speakerProfileDao.getAllProfiles()
    }

    override suspend fun createProfile(name: String): SpeakerProfile = withContext(Dispatchers.IO) {
        val profile = SpeakerProfile(
            id = UUID.randomUUID().toString(),
            name = name,
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis()
        )
        speakerProfileDao.insertProfile(profile)
        profile
    }

    override suspend fun updateProfile(profile: SpeakerProfile) = withContext(Dispatchers.IO) {
        speakerProfileDao.updateProfile(profile.copy(updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteProfile(profileId: String) = withContext(Dispatchers.IO) {
        speakerProfileDao.deleteProfile(profileId)
    }

    override suspend fun getProfileById(profileId: String): SpeakerProfile? = withContext(Dispatchers.IO) {
        speakerProfileDao.getProfileById(profileId)
    }

    override suspend fun matchSpeaker(audioData: ByteArray): String? = withContext(Dispatchers.IO) {
        // TODO: Implement speaker matching using ML model
        null
    }

    override suspend fun cleanup() {
        // No cleanup needed for this implementation
    }
} 