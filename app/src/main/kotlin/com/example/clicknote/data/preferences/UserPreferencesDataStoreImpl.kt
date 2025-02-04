package com.example.clicknote.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.example.clicknote.domain.model.CloudStorageType
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesDataStoreImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferencesDataStore {

    private object PreferencesKeys {
        val TRANSCRIPTION_LANGUAGE = stringPreferencesKey("transcription_language")
        val CLOUD_STORAGE_TYPE = stringPreferencesKey("cloud_storage_type")
        val OPENAI_API_KEY = stringPreferencesKey("openai_api_key")
        val WEEKLY_TRANSCRIPTION_COUNT = intPreferencesKey("weekly_transcription_count")
    }

    override val transcriptionLanguage: Flow<TranscriptionLanguage> = context.dataStore.data
        .map { preferences ->
            val languageCode = preferences[PreferencesKeys.TRANSCRIPTION_LANGUAGE] ?: TranscriptionLanguage.AUTO_DETECT.code
            TranscriptionLanguage.fromCode(languageCode)
        }

    override val cloudStorageType: Flow<CloudStorageType> = context.dataStore.data
        .map { preferences ->
            val type = preferences[PreferencesKeys.CLOUD_STORAGE_TYPE] ?: CloudStorageType.NONE.name
            CloudStorageType.valueOf(type)
        }

    override val openaiApiKey: Flow<String> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.OPENAI_API_KEY] ?: ""
        }

    override val weeklyTranscriptionCount: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0
        }

    override suspend fun setTranscriptionLanguage(language: TranscriptionLanguage) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.TRANSCRIPTION_LANGUAGE] = language.code
        }
    }

    override suspend fun setCloudStorageType(type: CloudStorageType) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.CLOUD_STORAGE_TYPE] = type.name
        }
    }

    override suspend fun setOpenaiApiKey(key: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.OPENAI_API_KEY] = key
        }
    }

    override suspend fun incrementWeeklyTranscriptionCount() {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] = currentCount + 1
        }
    }

    override suspend fun resetWeeklyTranscriptionCount() {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] = 0
        }
    }

    override suspend fun getWeeklyTranscriptionCount(): Int {
        return context.dataStore.data.map { preferences ->
            preferences[PreferencesKeys.WEEKLY_TRANSCRIPTION_COUNT] ?: 0
        }.collect { it }
    }
}