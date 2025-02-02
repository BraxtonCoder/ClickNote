package com.example.clicknote.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.example.clicknote.data.preferences.DataStoreConstants
import com.example.clicknote.data.preferences.UserPreferencesDataStoreImpl
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(
    name = DataStoreConstants.USER_PREFERENCES
)

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> = context.dataStore

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        dataStore: DataStore<Preferences>
    ): UserPreferencesDataStore = UserPreferencesDataStoreImpl(dataStore)
} 