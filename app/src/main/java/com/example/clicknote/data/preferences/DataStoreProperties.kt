package com.example.clicknote.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore

object DataStoreProperties {
    val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
    val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")
} 