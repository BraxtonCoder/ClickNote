package com.example.clicknote.service

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.media.projection.MediaProjectionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternalAudioCaptureManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording

    fun requestMediaProjectionPermission(activity: Activity) {
        val projectionManager = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        activity.startActivityForResult(
            projectionManager.createScreenCaptureIntent(),
            REQUEST_MEDIA_PROJECTION
        )
    }

    fun startRecording(resultCode: Int, data: Intent?) {
        if (resultCode != Activity.RESULT_OK || data == null) return
        
        val intent = Intent(context, InternalAudioCaptureService::class.java).apply {
            action = InternalAudioCaptureService.ACTION_START_RECORDING
            putExtra(InternalAudioCaptureService.EXTRA_RESULT_DATA, data)
        }
        context.startForegroundService(intent)
        _isRecording.value = true
    }

    fun stopRecording() {
        val intent = Intent(context, InternalAudioCaptureService::class.java).apply {
            action = InternalAudioCaptureService.ACTION_STOP_RECORDING
        }
        context.startService(intent)
        _isRecording.value = false
    }

    companion object {
        const val REQUEST_MEDIA_PROJECTION = 1000
    }
} 