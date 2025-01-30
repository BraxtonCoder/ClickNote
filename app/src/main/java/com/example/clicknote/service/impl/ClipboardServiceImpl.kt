package com.example.clicknote.service.impl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import com.example.clicknote.domain.model.Note
import com.example.clicknote.service.ClipboardManager
import com.example.clicknote.service.ClipboardService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClipboardServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val clipboardManager: ClipboardManager
) : ClipboardService {

    private val _clipboardContent = MutableStateFlow<String?>(null)

    override fun copyNote(note: Note) {
        val formattedText = formatNote(note)
        clipboardManager.copyText(formattedText)
        _clipboardContent.value = formattedText
    }

    override fun copyNotes(notes: List<Note>) {
        val formattedText = notes.joinToString("\n\n") { formatNote(it) }
        clipboardManager.copyText(formattedText)
        _clipboardContent.value = formattedText
    }

    override fun getLastCopiedText(): String? {
        return clipboardManager.getText()
    }

    override fun observeClipboard(): Flow<String?> = _clipboardContent.asStateFlow()

    override fun cleanup() {
        _clipboardContent.value = null
    }

    private fun formatNote(note: Note): String {
        val timestamp = note.timestamp?.let { "Created: $it\n" } ?: ""
        return buildString {
            append(timestamp)
            append(note.content)
        }
    }

    override fun copyToClipboard(text: String) {
        val clip = ClipData.newPlainText("Note", text)
        clipboardManager.setPrimaryClip(clip)
    }

    override fun getFromClipboard(): String? {
        return clipboardManager.primaryClip?.getItemAt(0)?.text?.toString()
    }

    override fun shareText(text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            type = "text/plain"
        }

        val shareIntent = Intent.createChooser(sendIntent, title)
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    override fun copyMultipleNotes(notes: List<String>) {
        val combinedText = notes.joinToString("\n\n")
        copyToClipboard(combinedText)
    }
} 