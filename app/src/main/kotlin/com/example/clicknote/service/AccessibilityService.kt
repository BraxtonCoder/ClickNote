package com.example.clicknote.service

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.clicknote.domain.repository.PreferencesRepository
import com.example.clicknote.service.VibrationHandler
import com.example.clicknote.service.VolumeButtonHandler
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class ClickNoteAccessibilityService : AccessibilityService() {

    @Inject
    lateinit var volumeButtonHandler: VolumeButtonHandler

    @Inject
    lateinit var vibrationHandler: VibrationHandler

    @Inject
    lateinit var preferencesRepository: PreferencesRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    override fun onServiceConnected() {
        super.onServiceConnected()
        volumeButtonHandler.startListening()
        volumeButtonHandler.setVolumeButtonCallback { isVolumeUp ->
            serviceScope.launch {
                if (preferencesRepository.isVibrationEnabled().first()) {
                    vibrationHandler.vibrateOnce()
                }
                // Handle recording state toggle
                // This will be implemented when we create the recording service
            }
        }
    }

    override fun onKeyEvent(event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) return false

        return when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                volumeButtonHandler.onKeyEvent(event.keyCode, event)
            }
            else -> false
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used in this service
    }

    override fun onInterrupt() {
        // Clean up resources
        volumeButtonHandler.cleanup()
        vibrationHandler.cancel()
    }

    override fun onDestroy() {
        super.onDestroy()
        volumeButtonHandler.cleanup()
        vibrationHandler.cancel()
    }
} 