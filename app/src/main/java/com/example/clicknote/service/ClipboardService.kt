package com.example.clicknote.service

import com.example.clicknote.domain.model.Note
import kotlinx.coroutines.flow.Flow

interface ClipboardService {
    /**
     * Copy text to clipboard
     */
    fun copyToClipboard(text: String)

    /**
     * Get text from clipboard
     */
    fun getFromClipboard(): String?

    /**
     * Share text using system share sheet
     */
    fun shareText(text: String, title: String = "Share Note")

    /**
     * Copy multiple notes as a single text with proper formatting
     */
    fun copyMultipleNotes(notes: List<String>)

    fun copyNote(note: Note)
    fun copyNotes(notes: List<Note>)
    fun getLastCopiedText(): String?
    fun observeClipboard(): Flow<String?>
    fun cleanup()
} 