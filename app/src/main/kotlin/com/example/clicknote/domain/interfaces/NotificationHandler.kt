package com.example.clicknote.domain.interfaces

import android.app.Notification
import com.example.clicknote.domain.model.TranscriptionState

interface NotificationHandler {
    fun createTranscriptionNotification(state: TranscriptionState): Notification
    fun showTranscriptionNotification(text: String)
    fun updateTranscriptionNotification(state: TranscriptionState)
    fun cancelTranscriptionNotification()
    fun createSilentNotification(text: String): Notification
    fun showSilentNotification(text: String, id: Int)
    fun cancelSilentNotification(id: Int)
    fun cancelAllNotifications()
    fun showRecordingNotification()
    fun hideRecordingNotification()
    fun showErrorNotification(message: String)
    fun clearAllNotifications()
} 