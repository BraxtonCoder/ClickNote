package com.example.clicknote.ui.viewmodel

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class Settings(
    val saveAudio: Boolean = true,
    val darkTheme: Boolean = false,
    val isSignedIn: Boolean = false,
    val isPremium: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {
    private object PreferencesKeys {
        val SAVE_AUDIO = booleanPreferencesKey("save_audio")
        val DARK_THEME = booleanPreferencesKey("dark_theme")
        val IS_SIGNED_IN = booleanPreferencesKey("is_signed_in")
        val IS_PREMIUM = booleanPreferencesKey("is_premium")
    }

    val settings = context.dataStore.data
        .catch { exception ->
            emit(emptyPreferences())
        }
        .map { preferences ->
            Settings(
                saveAudio = preferences[PreferencesKeys.SAVE_AUDIO] ?: true,
                darkTheme = preferences[PreferencesKeys.DARK_THEME] ?: false,
                isSignedIn = preferences[PreferencesKeys.IS_SIGNED_IN] ?: false,
                isPremium = preferences[PreferencesKeys.IS_PREMIUM] ?: false
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = Settings()
        )

    fun updateSaveAudio(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.SAVE_AUDIO] = enabled
            }
        }
    }

    fun updateDarkTheme(enabled: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.DARK_THEME] = enabled
            }
        }
    }

    fun updateSignInStatus(isSignedIn: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.IS_SIGNED_IN] = isSignedIn
            }
        }
    }

    fun updatePremiumStatus(isPremium: Boolean) {
        viewModelScope.launch {
            context.dataStore.edit { preferences ->
                preferences[PreferencesKeys.IS_PREMIUM] = isPremium
            }
        }
    }
} 