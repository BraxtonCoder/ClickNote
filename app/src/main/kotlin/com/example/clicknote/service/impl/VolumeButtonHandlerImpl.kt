package com.example.clicknote.service.impl

import android.content.Context
import android.view.KeyEvent
import com.example.clicknote.service.VolumeButtonHandler
import com.example.clicknote.service.VibrationHandler
import com.example.clicknote.service.RecordingManager
import com.example.clicknote.domain.service.VolumeButtonHandler
import com.example.clicknote.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VolumeButtonHandlerImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @ApplicationScope private val coroutineScope: CoroutineScope,
    private val vibrationHandler: VibrationHandler,
    private val recordingManager: RecordingManager
) : VolumeButtonHandler {

    private var lastKeyEventTime = 0L
    private var lastKeyCode = 0
    private var sequenceJob: Job? = null
    private val sequenceTimeout = 750L // ms
    private val _buttonState = MutableStateFlow<ButtonState>(ButtonState.Idle)
    private val buttonState: StateFlow<ButtonState> = _buttonState.asStateFlow()

    sealed class ButtonState {
        object Idle : ButtonState()
        object FirstPress : ButtonState()
        object SequenceDetected : ButtonState()
        object Error : ButtonState()
    }

    override fun onKeyEvent(keyCode: Int, event: KeyEvent): Boolean {
        if (event.action != KeyEvent.ACTION_DOWN) {
            return false
        }

        return try {
            when (keyCode) {
                KeyEvent.KEYCODE_VOLUME_UP, KeyEvent.KEYCODE_VOLUME_DOWN -> {
                    val currentTime = System.currentTimeMillis()
                    
                    when {
                        currentTime - lastKeyEventTime > sequenceTimeout -> {
                            // Start new sequence
                            lastKeyEventTime = currentTime
                            lastKeyCode = keyCode
                            _buttonState.value = ButtonState.FirstPress
                            startSequenceTimer()
                            false // Let system handle single volume press
                        }
                        lastKeyCode != keyCode -> {
                            // Valid sequence detected
                            sequenceJob?.cancel()
                            _buttonState.value = ButtonState.SequenceDetected
                            handleVolumeButtonSequence()
                            true // Consume the event
                        }
                        else -> false
                    }
                }
                else -> false
            }
        } catch (e: Exception) {
            _buttonState.value = ButtonState.Error
            false
        }
    }

    private fun startSequenceTimer() {
        sequenceJob?.cancel()
        sequenceJob = coroutineScope.launch {
            try {
                delay(sequenceTimeout)
                // Reset sequence if timeout reached
                resetState()
            } catch (e: CancellationException) {
                // Timer was cancelled, no action needed
            } catch (e: Exception) {
                _buttonState.value = ButtonState.Error
            }
        }
    }

    private fun handleVolumeButtonSequence() {
        coroutineScope.launch {
            try {
                val isRecording = recordingManager.isRecording.first()
                if (isRecording) {
                    vibrationHandler.vibrateDouble()
                    recordingManager.stopRecording()
                } else {
                    vibrationHandler.vibrateOnce()
                    recordingManager.startRecording()
                }
            } catch (e: Exception) {
                _buttonState.value = ButtonState.Error
            } finally {
                resetState()
            }
        }
    }

    private fun resetState() {
        lastKeyEventTime = 0
        lastKeyCode = 0
        _buttonState.value = ButtonState.Idle
    }

    override fun cleanup() {
        sequenceJob?.cancel()
        sequenceJob = null
        resetState()
    }
} 