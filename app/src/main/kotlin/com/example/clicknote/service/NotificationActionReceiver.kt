package com.example.clicknote.service

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface NotificationActionReceiverEntryPoint {
    fun clipboardManager(): ClipboardManager
}

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_COPY = "com.example.clicknote.COPY_NOTE"
        const val ACTION_SHARE = "com.example.clicknote.SHARE_NOTE"
        const val EXTRA_NOTE_ID = "note_id"
        const val EXTRA_CONTENT = "note_content"
    }

    private lateinit var clipboardManager: ClipboardManager

    override fun onReceive(context: Context, intent: Intent) {
        val entryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            NotificationActionReceiverEntryPoint::class.java
        )
        clipboardManager = entryPoint.clipboardManager()

        val content = intent.getStringExtra(EXTRA_CONTENT) ?: return
        val noteId = intent.getStringExtra(EXTRA_NOTE_ID)

        when (intent.action) {
            ACTION_COPY -> {
                val clip = ClipData.newPlainText("Note Content", content)
                clipboardManager.setPrimaryClip(clip)
                Toast.makeText(context, "Note copied to clipboard", Toast.LENGTH_SHORT).show()
            }
            ACTION_SHARE -> {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, content)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Note").apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                })
            }
        }
    }
} 