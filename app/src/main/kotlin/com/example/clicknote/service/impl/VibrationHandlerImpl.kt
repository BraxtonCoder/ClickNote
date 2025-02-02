package com.example.clicknote.service.impl

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.clicknote.domain.repository.PreferencesRepository
import com.example.clicknote.service.VibrationHandler
import com.example.clicknote.service.VibrationPattern
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VibrationHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesRepository: PreferencesRepository
) : VibrationHandler {

    private val vibrator: Vibrator by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun vibrate(pattern: VibrationPattern) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            when (pattern) {
                VibrationPattern.SINGLE_SHORT -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                VibrationPattern.DOUBLE_SHORT -> {
                    vibrator.vibrate(VibrationEffect.createWaveform(
                        longArrayOf(0, 50, 100, 50),
                        intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE),
                        -1
                    ))
                }
                VibrationPattern.LONG -> {
                    vibrator.vibrate(VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE))
                }
                VibrationPattern.ERROR -> {
                    vibrator.vibrate(VibrationEffect.createWaveform(
                        longArrayOf(0, 100, 100, 100),
                        intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0, VibrationEffect.DEFAULT_AMPLITUDE),
                        -1
                    ))
                }
            }
        } else {
            @Suppress("DEPRECATION")
            when (pattern) {
                VibrationPattern.SINGLE_SHORT -> vibrator.vibrate(50)
                VibrationPattern.DOUBLE_SHORT -> vibrator.vibrate(longArrayOf(0, 50, 100, 50), -1)
                VibrationPattern.LONG -> vibrator.vibrate(200)
                VibrationPattern.ERROR -> vibrator.vibrate(longArrayOf(0, 100, 100, 100), -1)
            }
        }
    }

    override fun cleanup() {
        vibrator.cancel()
    }

    override suspend fun vibrateRecordingStart() {
        if (!preferencesRepository.isVibrationEnabled().first()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }

    override suspend fun vibrateRecordingStop() {
        if (!preferencesRepository.isVibrationEnabled().first()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 100, 100), -1)
        }
    }

    override suspend fun vibratePremiumRequired() {
        if (!preferencesRepository.isVibrationEnabled().first()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 200, 100, 200), -1)
        }
    }

    override suspend fun vibrateError() {
        if (!preferencesRepository.isVibrationEnabled().first()) return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    override fun vibrate(pattern: LongArray) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(pattern, -1)
        }
    }

    override fun vibrateOnce(duration: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    override fun vibrateDouble() {
        vibrate(longArrayOf(0, 100, 100, 100))
    }

    override fun cancel() {
        vibrator.cancel()
    }
} 