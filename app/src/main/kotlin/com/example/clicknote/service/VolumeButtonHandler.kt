package com.example.clicknote.service

import android.view.KeyEvent

interface VolumeButtonHandler {
    fun handleVolumeButton(event: KeyEvent): Boolean
    fun vibrateSingle()
    fun vibrateDouble()
    fun vibrateError()
} 