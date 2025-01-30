package com.example.clicknote.service.impl

import android.content.Context
import android.view.KeyEvent
import com.example.clicknote.service.VolumeButtonHandler
import com.example.clicknote.service.VibrationHandler
import com.example.clicknote.service.RecordingManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VolumeButtonHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val vibrationHandler: VibrationHandler,
    private val recordingManager: RecordingManager
) : VolumeButtonHandler {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var lastKeyEventTime = 0L
    private var lastKeyCode = 0
    private var sequenceJob: Job? = null
    private val sequenceTimeout = 750L // ms

    override fun onKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        when (keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                val currentTime = System.currentTimeMillis()
                
                if (currentTime - lastKeyEventTime > sequenceTimeout) {
                    // Start new sequence
                    lastKeyEventTime = currentTime
                    lastKeyCode = keyCode
                    startSequenceTimer()
                    return false // Let system handle single volume press
                } else if (lastKeyCode != keyCode) {
                    // Valid sequence detected
                    sequenceJob?.cancel()
                    handleVolumeButtonSequence()
                    return true // Consume the event
                }
            }
        }
        return false
    }

    private fun startSequenceTimer() {
        sequenceJob?.cancel()
        sequenceJob = scope.launch {
            delay(sequenceTimeout)
            // Reset sequence if timeout reached
            lastKeyEventTime = 0
            lastKeyCode = 0
        }
    }

    private fun handleVolumeButtonSequence() {
        scope.launch {
            val isRecording = recordingManager.isRecording.first()
            if (isRecording) {
                vibrationHandler.vibrateDouble()
                recordingManager.stopRecording()
            } else {
                vibrationHandler.vibrateOnce()
                recordingManager.startRecording()
            }
        }
    }

    override fun cleanup() {
        sequenceJob?.cancel()
        sequenceJob = null
    }
} 