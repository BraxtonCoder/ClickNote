package com.example.clicknote.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.example.clicknote.data.preferences.UserPreferencesDataStoreImpl
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.ClickNoteApplication
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> =
        (context.applicationContext as ClickNoteApplication).dataStore

    @Provides
    @Singleton
    fun provideUserPreferencesDataStore(dataStore: DataStore<Preferences>): UserPreferencesDataStore =
        UserPreferencesDataStoreImpl(dataStore)
}