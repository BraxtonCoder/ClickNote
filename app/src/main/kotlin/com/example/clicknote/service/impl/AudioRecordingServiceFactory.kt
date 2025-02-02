package com.example.clicknote.service.impl

import android.content.Context
import android.content.Intent
import com.example.clicknote.domain.interfaces.RecordingManager
import com.example.clicknote.domain.service.NotificationHandler
import javax.inject.Inject

class AudioRecordingServiceFactory @Inject constructor(
    private val context: Context,
    private val notificationHandler: NotificationHandler,
    private val recordingManager: RecordingManager
) {
    fun startService(action: String) {
        val intent = Intent(context, AudioRecordingForegroundService::class.java).apply {
            this.action = action
        }
        context.startService(intent)
    }

    fun stopService() {
        context.stopService(Intent(context, AudioRecordingForegroundService::class.java))
    }
} 