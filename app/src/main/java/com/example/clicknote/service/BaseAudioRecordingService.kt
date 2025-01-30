package com.example.clicknote.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
abstract class BaseAudioRecordingService : Service() {
    
    companion object {
        const val ACTION_START_RECORDING = "com.example.clicknote.action.START_RECORDING"
        const val ACTION_STOP_RECORDING = "com.example.clicknote.action.STOP_RECORDING"
        const val ACTION_PAUSE_RECORDING = "com.example.clicknote.action.PAUSE_RECORDING"
        const val ACTION_RESUME_RECORDING = "com.example.clicknote.action.RESUME_RECORDING"
        const val ACTION_CANCEL_RECORDING = "com.example.clicknote.action.CANCEL_RECORDING"
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START_RECORDING -> handleStartRecording()
            ACTION_STOP_RECORDING -> handleStopRecording()
            ACTION_PAUSE_RECORDING -> handlePauseRecording()
            ACTION_RESUME_RECORDING -> handleResumeRecording()
            ACTION_CANCEL_RECORDING -> handleCancelRecording()
        }
        return START_NOT_STICKY
    }

    protected abstract fun handleStartRecording()
    protected abstract fun handleStopRecording()
    protected abstract fun handlePauseRecording()
    protected abstract fun handleResumeRecording()
    protected abstract fun handleCancelRecording()
} 