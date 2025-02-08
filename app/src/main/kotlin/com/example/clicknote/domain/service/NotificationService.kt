package com.example.clicknote.domain.service

import android.app.Notification
import com.example.clicknote.domain.model.Note

interface NotificationService {
    fun showTranscriptionNotification(note: Note)
    fun showSilentNotification(note: Note)
    fun showErrorNotification(message: String)
    fun showProgressNotification(progress: Int)
    fun cancelNotification(id: Int)
    fun cancelAllNotifications()
    fun createForegroundNotification(): Notification
    fun updateForegroundNotification(progress: Int)
} 