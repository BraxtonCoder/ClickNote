package com.example.clicknote.service.impl

import android.accessibilityservice.AccessibilityService
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import com.example.clicknote.service.VolumeButtonHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AccessibilityServiceImpl : AccessibilityService() {

    @Inject
    lateinit var volumeButtonHandler: VolumeButtonHandler

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used in this implementation
    }

    override fun onInterrupt() {
        // Not used in this implementation
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        event?.let {
            return volumeButtonHandler.onKeyEvent(it.keyCode, it)
        }
        return super.onKeyEvent(event)
    }

    override fun onDestroy() {
        super.onDestroy()
        volumeButtonHandler.cleanup()
    }
} 