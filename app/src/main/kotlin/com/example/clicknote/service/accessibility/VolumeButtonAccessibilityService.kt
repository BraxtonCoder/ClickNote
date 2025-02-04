package com.example.clicknote.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.clicknote.service.RecordingManager
import com.example.clicknote.service.VolumeButtonHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class VolumeButtonAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var volumeButtonHandler: VolumeButtonHandler

    @Inject
    lateinit var recordingManager: RecordingManager
    
    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var lastVolumeUpTime = 0L
    private var lastVolumeDownTime = 0L
    private val triggerWindow = 750L // 750ms window for sequential clicks

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.launch {
            stopRecording()
        }
        serviceScope.cancel()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used
    }

    override fun onInterrupt() {
        serviceScope.launch {
            stopRecording()
        }
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        event ?: return false

        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastVolumeDownTime < triggerWindow) {
                        handleVolumeButtonTrigger()
                        return true
                    }
                    lastVolumeUpTime = currentTime
                }
                return volumeButtonHandler.handleVolumeButton(event)
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastVolumeUpTime < triggerWindow) {
                        handleVolumeButtonTrigger()
                        return true
                    }
                    lastVolumeDownTime = currentTime
                }
                return volumeButtonHandler.handleVolumeButton(event)
            }
        }
        return false
    }

    private fun handleVolumeButtonTrigger() {
        serviceScope.launch {
            toggleRecording()
        }
    }

    private suspend fun toggleRecording() {
        val isRecording = recordingManager.isRecording.first()
        if (isRecording) {
            stopRecording()
            volumeButtonHandler.vibrateDouble()
        } else {
            if (recordingManager.canStartRecording()) {
                recordingManager.startRecording()
                volumeButtonHandler.vibrateSingle()
            } else {
                volumeButtonHandler.vibrateError()
            }
        }
    }

    private suspend fun stopRecording() {
        recordingManager.stopRecording()
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Initialize any necessary setup
    }
} 