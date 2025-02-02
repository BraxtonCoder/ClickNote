package com.example.clicknote.data.repository

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import com.example.clicknote.domain.model.TranscriptionLanguage
import com.example.clicknote.domain.repository.PreferencesRepository
import com.example.clicknote.data.preferences.UserPreferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class PreferencesRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val dataStore: UserPreferencesDataStore
) : PreferencesRepository {

    private val TRANSCRIPTION_LANGUAGE = stringPreferencesKey("app_settings_transcription_language")

    override suspend fun setTranscriptionLanguage(language: TranscriptionLanguage) {
        dataStore.setTranscriptionLanguage(language.code)
    }

    override fun getTranscriptionLanguage(): Flow<TranscriptionLanguage> {
        return dataStore.transcriptionLanguage.map { code ->
            TranscriptionLanguage.fromCode(code)
        }
    }
}