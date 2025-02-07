package com.example.clicknote.data.model

import android.util.Log
import com.example.clicknote.domain.model.SpeechModel

class VoskModel(override val path: String) : SpeechModel {
    private var isInitialized = false

    init {
        try {
            System.loadLibrary("vosk")
            isInitialized = true
        } catch (e: UnsatisfiedLinkError) {
            Log.e(TAG, "Failed to load vosk library", e)
        }
    }

    override fun isLoaded(): Boolean = isInitialized

    override fun close() {
        // Will be implemented when Vosk dependency is properly set up
    }

    companion object {
        private const val TAG = "VoskModel"
    }
} 