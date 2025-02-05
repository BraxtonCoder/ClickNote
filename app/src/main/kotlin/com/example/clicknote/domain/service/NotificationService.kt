package com.example.clicknote.domain.service

import com.example.clicknote.domain.model.Note

interface NotificationService {
    fun showTranscriptionNotification(note: Note)
    fun showRecordingNotification(isRecording: Boolean)
    fun showSyncNotification(isSyncing: Boolean)
    fun showErrorNotification(message: String)
    fun cancelNotification(id: Int)
    fun cancelAllNotifications()
} 