package com.example.clicknote.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.view.KeyEvent
import com.example.clicknote.service.VolumeButtonHandler
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface VolumeButtonReceiverEntryPoint {
    fun volumeButtonHandler(): VolumeButtonHandler
}

class VolumeButtonReceiver : BroadcastReceiver() {

    private lateinit var volumeButtonHandler: VolumeButtonHandler

    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            VolumeButtonReceiverEntryPoint::class.java
        )
        volumeButtonHandler = entryPoint.volumeButtonHandler()

        if (intent?.action == Intent.ACTION_MEDIA_BUTTON) {
            val event = intent.getParcelableExtra<KeyEvent>(Intent.EXTRA_KEY_EVENT)
            event?.let {
                volumeButtonHandler.onKeyEvent(it.keyCode, it)
            }
        }
    }
} 