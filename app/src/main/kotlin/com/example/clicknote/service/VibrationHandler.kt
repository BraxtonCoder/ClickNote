package com.example.clicknote.service

interface VibrationHandler {
    fun vibrate(pattern: LongArray)
    fun vibrateOnce(duration: Long = 100)
    fun vibrateDouble()
    fun cleanup()
} 