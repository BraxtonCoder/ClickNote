package com.example.clicknote.domain.service

interface ClipboardService {
    fun copyText(text: String)
    fun copyTexts(texts: List<String>)
    fun getText(): String?
    fun hasText(): Boolean
    fun clear()
    fun addClipboardListener(listener: ClipboardListener)
    fun removeClipboardListener(listener: ClipboardListener)
}

interface ClipboardListener {
    fun onClipboardChanged(text: String?)
} 