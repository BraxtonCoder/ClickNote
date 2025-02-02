package com.example.clicknote.service.impl

import android.content.ClipData
import android.content.Context
import com.example.clicknote.service.ClipboardManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ClipboardManager {
    private val systemClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager

    override fun setPrimaryClip(clip: ClipData) {
        systemClipboardManager.setPrimaryClip(clip)
    }

    override fun getPrimaryClip(): ClipData? {
        return systemClipboardManager.primaryClip
    }

    override fun hasPrimaryClip(): Boolean {
        return systemClipboardManager.hasPrimaryClip()
    }

    override fun clearPrimaryClip() {
        systemClipboardManager.clearPrimaryClip()
    }
} 