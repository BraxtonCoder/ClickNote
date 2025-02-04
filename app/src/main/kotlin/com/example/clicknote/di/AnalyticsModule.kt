package com.example.clicknote.di

import android.content.Context
import com.example.clicknote.data.analytics.MixPanelAnalyticsService
import com.example.clicknote.domain.analytics.AnalyticsService
import com.mixpanel.android.mpmetrics.MixpanelAPI
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AnalyticsModule {
    companion object {
        private const val MIXPANEL_TOKEN = "a96f70206257896eabf7625522d7c8c9"
        private const val TRACK_AUTOMATIC_EVENTS = false

        @Provides
        @Singleton
        fun provideMixpanelAPI(@ApplicationContext context: Context): MixpanelAPI {
            return MixpanelAPI.getInstance(context, MIXPANEL_TOKEN, TRACK_AUTOMATIC_EVENTS)
        }
    }

    @Binds
    @Singleton
    abstract fun bindAnalyticsService(impl: MixPanelAnalyticsService): AnalyticsService
} 