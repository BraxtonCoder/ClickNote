package com.example.clicknote.service.impl

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.content.Context
import android.os.PowerManager
import com.example.clicknote.domain.interfaces.AudioRecordingService
import com.example.clicknote.domain.interfaces.RecordingState
import com.example.clicknote.domain.service.NotificationHandler
import com.example.clicknote.domain.service.RecordingManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AudioRecordingServiceImpl @Inject constructor(
    private val context: Context,
    private val notificationHandler: NotificationHandler,
    private val recordingManager: RecordingManager
) : AudioRecordingService {

    private val _recordingState = MutableStateFlow<RecordingState>(RecordingState.Idle)
    override val recordingState: StateFlow<RecordingState> = _recordingState.asStateFlow()
    override val isRecording: StateFlow<Boolean> = recordingManager.isRecording

    override fun startRecording() {
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            try {
                recordingManager.startRecording()
                notificationHandler.showRecordingNotification()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    override fun stopRecording() {
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            try {
                recordingManager.stopRecording()
                notificationHandler.hideRecordingNotification()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    override fun pauseRecording() {
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            try {
                recordingManager.pauseRecording()
                notificationHandler.updateNotificationForPausedState()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    override fun resumeRecording() {
        CoroutineScope(Dispatchers.Main + SupervisorJob()).launch {
            try {
                recordingManager.startRecording()
                notificationHandler.updateNotificationForRecordingState()
            } catch (e: Exception) {
                handleError(e)
            }
        }
    }

    private fun handleError(e: Exception) {
        // TODO: Implement error handling
    }
} 