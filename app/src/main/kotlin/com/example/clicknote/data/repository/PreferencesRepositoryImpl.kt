package com.example.clicknote.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import com.example.clicknote.domain.model.Language
import com.example.clicknote.domain.repository.PreferencesRepository
import com.example.clicknote.di.SettingsDataStore
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesRepositoryImpl @Inject constructor(
    @SettingsDataStore private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    companion object {
        private val OPENAI_API_KEY = stringPreferencesKey("app_settings_openai_api_key")
        private val TRANSCRIPTION_LANGUAGE = stringPreferencesKey("app_settings_transcription_language")
        private val ONLINE_TRANSCRIPTION_ENABLED = booleanPreferencesKey("app_settings_online_transcription_enabled")
        private val AUDIO_SAVING_ENABLED = booleanPreferencesKey("app_settings_audio_saving_enabled")
        private val NOTIFICATIONS_ENABLED = booleanPreferencesKey("app_settings_notifications_enabled")
        private val AUTO_SUMMARY_ENABLED = booleanPreferencesKey("app_settings_auto_summary_enabled")
        private val THEME = stringPreferencesKey("app_settings_theme")
    }

    override suspend fun setOpenAIApiKey(apiKey: String) {
        dataStore.edit { preferences ->
            preferences[OPENAI_API_KEY] = apiKey
        }
    }

    override suspend fun getOpenAIApiKey(): String {
        return dataStore.data.first()[OPENAI_API_KEY] ?: ""
    }

    override suspend fun setTranscriptionLanguage(language: Language) {
        dataStore.edit { preferences ->
            preferences[TRANSCRIPTION_LANGUAGE] = language.name
        }
    }

    override fun getTranscriptionLanguage(): Flow<Language> {
        return dataStore.data.map { preferences ->
            try {
                Language.valueOf(preferences[TRANSCRIPTION_LANGUAGE] ?: Language.ENGLISH.name)
            } catch (e: IllegalArgumentException) {
                Language.ENGLISH
            }
        }
    }

    override suspend fun setOnlineTranscriptionEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[ONLINE_TRANSCRIPTION_ENABLED] = enabled
        }
    }

    override fun isOnlineTranscriptionEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[ONLINE_TRANSCRIPTION_ENABLED] ?: true
        }
    }

    override suspend fun setAudioSavingEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUDIO_SAVING_ENABLED] = enabled
        }
    }

    override fun isAudioSavingEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[AUDIO_SAVING_ENABLED] ?: true
        }
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[NOTIFICATIONS_ENABLED] = enabled
        }
    }

    override fun areNotificationsEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[NOTIFICATIONS_ENABLED] ?: true
        }
    }

    override suspend fun setAutoSummaryEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[AUTO_SUMMARY_ENABLED] = enabled
        }
    }

    override fun isAutoSummaryEnabled(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[AUTO_SUMMARY_ENABLED] ?: true
        }
    }

    override suspend fun setTheme(theme: String) {
        dataStore.edit { preferences ->
            preferences[THEME] = theme
        }
    }

    override fun getTheme(): Flow<String> {
        return dataStore.data.map { preferences ->
            preferences[THEME] ?: "system"
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
} 