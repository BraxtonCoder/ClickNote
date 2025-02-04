package com.example.clicknote.di

import com.example.clicknote.di.qualifiers.ApplicationScope
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineScopeModule {

    @Singleton
    @ApplicationScope
    @Provides
    fun providesApplicationScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob())
    }
} 