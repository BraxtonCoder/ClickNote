package com.example.clicknote.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferencesImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : UserPreferences {

    override fun getWeeklyUsageCount(): Flow<Int> = context.dataStore.data
        .map { preferences -> preferences[WEEKLY_USAGE_COUNT] ?: 0 }

    override suspend fun incrementWeeklyUsageCount() {
        context.dataStore.edit { preferences ->
            val currentCount = preferences[WEEKLY_USAGE_COUNT] ?: 0
            preferences[WEEKLY_USAGE_COUNT] = currentCount + 1
        }
    }

    override suspend fun resetWeeklyUsageCount() {
        context.dataStore.edit { preferences ->
            preferences[WEEKLY_USAGE_COUNT] = 0
        }
    }

    override fun getLastWeeklyResetDate(): Flow<Long> = context.dataStore.data
        .map { preferences -> preferences[LAST_WEEKLY_RESET_DATE] ?: 0L }

    override suspend fun updateLastWeeklyResetDate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_WEEKLY_RESET_DATE] = timestamp
        }
    }

    override fun getSubscriptionId(): Flow<String?> = context.dataStore.data
        .map { preferences -> preferences[SUBSCRIPTION_ID] }

    override suspend fun setSubscriptionId(id: String?) {
        context.dataStore.edit { preferences ->
            if (id != null) {
                preferences[SUBSCRIPTION_ID] = id
            } else {
                preferences.remove(SUBSCRIPTION_ID)
            }
        }
    }

    override fun isSubscriptionActive(): Flow<Boolean> = context.dataStore.data
        .map { preferences -> preferences[IS_SUBSCRIPTION_ACTIVE] ?: false }

    override suspend fun setSubscriptionActive(active: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_SUBSCRIPTION_ACTIVE] = active
        }
    }

    companion object {
        private val WEEKLY_USAGE_COUNT = intPreferencesKey("weekly_usage_count")
        private val LAST_WEEKLY_RESET_DATE = longPreferencesKey("last_weekly_reset_date")
        private val SUBSCRIPTION_ID = stringPreferencesKey("subscription_id")
        private val IS_SUBSCRIPTION_ACTIVE = booleanPreferencesKey("is_subscription_active")
    }
} 