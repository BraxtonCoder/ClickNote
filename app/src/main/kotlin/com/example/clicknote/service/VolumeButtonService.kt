package com.example.clicknote.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VolumeButtonService : AccessibilityService() {
    @Inject
    lateinit var vibrator: Vibrator

    private var lastVolumeUpTime = 0L
    private var lastVolumeDownTime = 0L
    private var isRecording = false
    private val handler = Handler(Looper.getMainLooper())
    private val triggerWindow = 750L // 750ms window for sequential presses

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        val currentTime = System.currentTimeMillis()
        
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                lastVolumeUpTime = currentTime
                checkTrigger(currentTime)
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                lastVolumeDownTime = currentTime
                checkTrigger(currentTime)
            }
        }

        // Let the system handle single button presses normally
        return false
    }

    private fun checkTrigger(currentTime: Long) {
        val timeDiff = kotlin.math.abs(lastVolumeUpTime - lastVolumeDownTime)
        
        if (timeDiff <= triggerWindow) {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
            // Reset times to prevent double triggers
            lastVolumeUpTime = 0
            lastVolumeDownTime = 0
        } else {
            // Reset after window expires
            handler.postDelayed({
                if (currentTime - lastVolumeUpTime >= triggerWindow && 
                    currentTime - lastVolumeDownTime >= triggerWindow) {
                    lastVolumeUpTime = 0
                    lastVolumeDownTime = 0
                }
            }, triggerWindow)
        }
    }

    private fun startRecording() {
        isRecording = true
        vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        // Broadcast intent to start recording
        sendBroadcast(Intent(ACTION_START_RECORDING))
    }

    private fun stopRecording() {
        isRecording = false
        // Double vibration for stop
        vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
        // Broadcast intent to stop recording
        sendBroadcast(Intent(ACTION_STOP_RECORDING))
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}

    companion object {
        const val ACTION_START_RECORDING = "com.example.clicknote.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.clicknote.STOP_RECORDING"
    }
} 