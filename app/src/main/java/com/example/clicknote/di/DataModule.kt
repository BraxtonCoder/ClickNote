package com.example.clicknote.di

import com.example.clicknote.data.preferences.UserPreferencesDataStoreImpl
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {
    @Binds
    @Singleton
    abstract fun bindUserPreferencesDataStore(impl: UserPreferencesDataStoreImpl): UserPreferencesDataStore
} 