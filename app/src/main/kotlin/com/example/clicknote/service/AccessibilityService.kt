package com.example.clicknote.service

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.service.VibrationHandler
import com.example.clicknote.service.VolumeButtonHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import javax.inject.Inject

@AndroidEntryPoint
class ClickNoteAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var volumeButtonHandler: VolumeButtonHandler

    @Inject
    lateinit var vibrationHandler: VibrationHandler

    @Inject
    lateinit var userPreferences: UserPreferencesDataStore

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        
        // Initialize volume button handling
        serviceScope.launch {
            val isVibrationEnabled = userPreferences.vibrationEnabled.first()
            if (isVibrationEnabled) {
                vibrationHandler.vibrateOnce()
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                volumeButtonHandler.handleVolumeButton(event)
            }
            else -> false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used in this service
    }

    override fun onInterrupt() {
        // Clean up resources
        vibrationHandler.cleanup()
        serviceScope.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        vibrationHandler.cleanup()
        serviceScope.cancel()
    }
} 