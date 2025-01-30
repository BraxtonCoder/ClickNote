package com.example.clicknote.service.accessibility

import android.accessibilityservice.AccessibilityService
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import android.app.NotificationManager
import com.example.clicknote.data.repository.NoteRepository
import com.example.clicknote.data.repository.UserPreferencesDataStore
import com.example.clicknote.service.AudioRecorder
import com.example.clicknote.service.NotificationHandler
import com.example.clicknote.service.RecordingManager
import com.example.clicknote.service.WhisperService
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VolumeButtonAccessibilityServiceEntryPoint {
    fun recordingManager(): RecordingManager
    fun audioRecorder(): AudioRecorder
    fun whisperService(): WhisperService
    fun noteRepository(): NoteRepository
    fun notificationHandler(): NotificationHandler
    fun userPreferences(): UserPreferencesDataStore
    fun vibrator(): Vibrator
    fun notificationManager(): NotificationManager
}

class VolumeButtonAccessibilityService : AccessibilityService() {

    private lateinit var recordingManager: RecordingManager
    private lateinit var audioRecorder: AudioRecorder
    private lateinit var whisperService: WhisperService
    private lateinit var noteRepository: NoteRepository
    private lateinit var notificationHandler: NotificationHandler
    private lateinit var userPreferences: UserPreferencesDataStore
    private lateinit var vibrator: Vibrator
    private lateinit var notificationManager: NotificationManager

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var lastVolumeUpTime = 0L
    private var lastVolumeDownTime = 0L
    private val triggerWindow = 750L // 750ms window for sequential clicks

    override fun onCreate() {
        super.onCreate()
        val entryPoint = EntryPointAccessors.fromApplication(
            applicationContext,
            VolumeButtonAccessibilityServiceEntryPoint::class.java
        )
        recordingManager = entryPoint.recordingManager()
        audioRecorder = entryPoint.audioRecorder()
        whisperService = entryPoint.whisperService()
        noteRepository = entryPoint.noteRepository()
        notificationHandler = entryPoint.notificationHandler()
        userPreferences = entryPoint.userPreferences()
        vibrator = entryPoint.vibrator()
        notificationManager = entryPoint.notificationManager()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        // Not used
    }

    override fun onInterrupt() {
        scope.launch {
            recordingManager.stopRecording()
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
            }
        }

        // Let the system handle volume changes if no trigger detected
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        when (event.keyCode) {
            KeyEvent.KEYCODE_VOLUME_UP -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_SHOW_UI)
                }
                return true
            }
            KeyEvent.KEYCODE_VOLUME_DOWN -> {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    audioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_SHOW_UI)
                }
                return true
            }
        }

        return false
    }

    private fun handleVolumeButtonTrigger() {
        scope.launch {
            val isRecording = recordingManager.isRecording.first()
            if (isRecording) {
                recordingManager.stopRecording()
                vibrateDouble()
            } else {
                if (recordingManager.canStartRecording()) {
                    recordingManager.startRecording()
                    vibrateSingle()
                } else {
                    // Free user has exceeded weekly limit
                    vibrateError()
                }
            }
        }
    }

    private fun vibrateSingle(duration: Long = 100) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(duration, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(duration)
        }
    }

    private fun vibrateDouble() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 100, 100), -1)
        }
    }

    private fun vibrateError() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 100, 100, 100), -1))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 100, 100, 100, 100, 100), -1)
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // Initialize any necessary setup
    }
} 