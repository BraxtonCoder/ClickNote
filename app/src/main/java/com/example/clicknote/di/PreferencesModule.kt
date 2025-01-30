package com.example.clicknote.di

import com.example.clicknote.data.preferences.UserPreferencesDataStoreImpl
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import dagger.Lazy
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.clicknote.di.UserPreferencesDataStore as UserPreferencesDataStoreQualifier

@Module
@InstallIn(SingletonComponent::class)
object PreferencesModule {
    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(
        @UserPreferencesDataStoreQualifier dataStore: Lazy<DataStore<Preferences>>
    ): UserPreferencesDataStore {
        return UserPreferencesDataStoreImpl(dataStore)
    }
} 