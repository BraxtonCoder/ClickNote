package com.example.clicknote.service

import android.content.ClipData

interface ClipboardManager {
    fun setPrimaryClip(clip: ClipData)
    fun getPrimaryClip(): ClipData?
    fun hasPrimaryClip(): Boolean
    fun clearPrimaryClip()
} 