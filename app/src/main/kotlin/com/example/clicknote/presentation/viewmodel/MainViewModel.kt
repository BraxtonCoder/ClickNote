package com.example.clicknote.presentation.viewmodel

import androidx.lifecycle.ViewModel
import com.example.clicknote.data.analytics.MixPanelAnalyticsService
import com.example.clicknote.domain.analytics.AnalyticsService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val analyticsService: AnalyticsService
) : ViewModel() {

    fun onAppStart() {
        analyticsService.trackEvent(MixPanelAnalyticsService.EVENT_APP_OPENED)
    }

    fun onNoteCreated(noteType: String, lengthInSeconds: Int) {
        analyticsService.trackEvent(
            MixPanelAnalyticsService.EVENT_NOTE_CREATED,
            mapOf(
                MixPanelAnalyticsService.PROP_NOTE_TYPE to noteType,
                MixPanelAnalyticsService.PROP_NOTE_LENGTH to lengthInSeconds
            )
        )
    }

    fun onTranscriptionStarted(type: String) {
        analyticsService.trackEvent(
            MixPanelAnalyticsService.EVENT_TRANSCRIPTION_STARTED,
            mapOf(MixPanelAnalyticsService.PROP_TRANSCRIPTION_TYPE to type)
        )
    }

    fun onUserSignedIn(userId: String) {
        analyticsService.identify(userId)
        analyticsService.trackEvent(
            MixPanelAnalyticsService.EVENT_USER_SIGNED_IN,
            mapOf(MixPanelAnalyticsService.PROP_USER_ID to userId)
        )
    }
} 