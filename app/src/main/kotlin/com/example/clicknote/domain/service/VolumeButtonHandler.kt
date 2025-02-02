package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface VolumeButtonHandler {
    fun startListening()
    fun stopListening()
    fun isListening(): Boolean
    fun getButtonEvents(): Flow<VolumeButtonEvent>
    fun setTriggerWindow(milliseconds: Long)
    fun getTriggerWindow(): Long
    fun isInTriggerWindow(): Boolean
}

sealed class VolumeButtonEvent {
    object UpPressed : VolumeButtonEvent()
    object DownPressed : VolumeButtonEvent()
    object SequentialTrigger : VolumeButtonEvent()
    object SingleButton : VolumeButtonEvent()
    object Timeout : VolumeButtonEvent()
} 