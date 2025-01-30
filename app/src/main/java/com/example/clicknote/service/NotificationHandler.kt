package com.example.clicknote.service

import android.app.Notification
import com.example.clicknote.domain.model.Note

interface NotificationHandler {
    /**
     * Show recording in progress notification
     */
    fun showRecordingNotification(isRecording: Boolean)

    /**
     * Show transcription complete notification
     */
    fun showTranscriptionNotification(note: Note)

    /**
     * Show premium notification
     */
    fun showPremiumNotification()

    /**
     * Show error notification
     */
    fun showErrorNotification(message: String)

    /**
     * Show note created notification
     */
    fun showNoteCreatedNotification(note: Note)

    /**
     * Cancel recording notification
     */
    fun cancelRecordingNotification()

    /**
     * Cancel transcription notification
     */
    fun cancelTranscriptionNotification()

    /**
     * Cancel note notification
     */
    fun cancelNoteNotification(noteId: String)

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications()

    fun cancelNotification(id: Int)

    suspend fun cleanup()

    companion object {
        const val RECORDING_NOTIFICATION_ID = 1001
        const val TRANSCRIPTION_NOTIFICATION_ID = 1002
        const val ERROR_NOTIFICATION_ID = 1003
    }
} 