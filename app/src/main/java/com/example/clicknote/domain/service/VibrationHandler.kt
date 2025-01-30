package com.example.clicknote.domain.service

interface VibrationHandler {
    fun vibrate(pattern: VibrationPattern)
    fun cancelVibration()
    fun isVibrating(): Boolean
}

enum class VibrationPattern(val pattern: LongArray) {
    RECORD_START(longArrayOf(0, 100)),
    RECORD_STOP(longArrayOf(0, 100, 100, 100)),
    ERROR(longArrayOf(0, 200)),
    SUCCESS(longArrayOf(0, 50, 50, 50)),
    WARNING(longArrayOf(0, 100, 100, 100, 100, 100))
} 