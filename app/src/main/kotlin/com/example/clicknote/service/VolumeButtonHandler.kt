package com.example.clicknote.service

import android.view.KeyEvent
import kotlinx.coroutines.flow.Flow

interface VolumeButtonHandler {
    val isListening: Flow<Boolean>
    
    fun onKeyEvent(keyCode: Int, event: KeyEvent): Boolean
    fun cleanup()
    fun startListening()
    fun stopListening()
    fun onVolumeButtonPressed(isVolumeUp: Boolean)
    fun setVolumeButtonCallback(callback: (Boolean) -> Unit)
} 