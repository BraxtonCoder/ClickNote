package com.example.clicknote.util

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.clicknote.domain.model.ExportSettings
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.TranscriptionSegment
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.property.TextAlignment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class NoteExporter(private val context: Context) {
    
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    
    suspend fun exportAsPdf(
        notes: List<Pair<Note, List<TranscriptionSegment>>>,
        settings: ExportSettings = ExportSettings()
    ): Uri = withContext(Dispatchers.IO) {
        val fileName = if (notes.size == 1) {
            "Note_${notes[0].first.id}_${System.currentTimeMillis()}.pdf"
        } else {
            "Notes_${System.currentTimeMillis()}.pdf"
        }
        val pdfFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), fileName)
        
        PdfWriter(pdfFile).use { writer ->
            Document(com.itextpdf.kernel.pdf.PdfDocument(writer)).use { document ->
                notes.forEachIndexed { index, (note, segments) ->
                    if (index > 0) {
                        document.add(Paragraph("\n").setFontSize(settings.spacing.paragraphSpacing))
                    }
                    
                    // Add title
                    if (settings.includeTitle) {
                        document.add(
                            Paragraph(note.title ?: "Untitled Note")
                                .setFontSize(settings.fontSize.titleSize)
                                .setBold()
                                .setTextAlignment(TextAlignment.CENTER)
                        )
                    }
                    
                    // Add date
                    if (settings.includeDate) {
                        document.add(
                            Paragraph("Created: ${dateFormat.format(note.createdAt)}")
                                .setFontSize(settings.fontSize.timestampSize)
                                .setItalic()
                        )
                    }
                    
                    // Add transcription segments
                    segments.forEach { segment ->
                        val paragraph = Paragraph().setMultipliedLeading(settings.spacing.lineSpacing)
                        
                        if (settings.includeTimestamps) {
                            paragraph.add(
                                Paragraph("${formatTimestamp(segment.startTime)}")
                                    .setFontSize(settings.fontSize.timestampSize)
                                    .setItalic()
                            )
                        }
                        
                        paragraph.add(
                            Paragraph(segment.text)
                                .setFontSize(settings.fontSize.textSize)
                        )
                        
                        document.add(paragraph)
                    }
                    
                    // Add summary if available
                    if (settings.includeSummary && note.summary != null) {
                        document.add(
                            Paragraph("\nSummary")
                                .setFontSize(settings.fontSize.titleSize)
                                .setBold()
                        )
                        document.add(
                            Paragraph(note.summary)
                                .setFontSize(settings.fontSize.textSize)
                        )
                    }
                }
            }
        }
        
        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )
    }
    
    fun shareAsText(
        notes: List<Pair<Note, List<TranscriptionSegment>>>,
        settings: ExportSettings = ExportSettings()
    ): Intent {
        val shareText = buildString {
            notes.forEachIndexed { index, (note, segments) ->
                if (index > 0) appendLine("\n---\n")
                
                if (settings.includeTitle) {
                    appendLine(note.title ?: "Untitled Note")
                }
                
                if (settings.includeDate) {
                    appendLine("Created: ${dateFormat.format(note.createdAt)}")
                }
                appendLine()
                
                segments.forEach { segment ->
                    if (settings.includeTimestamps) {
                        appendLine("[${formatTimestamp(segment.startTime)}]")
                    }
                    appendLine(segment.text)
                    appendLine()
                }
                
                if (settings.includeSummary && note.summary != null) {
                    appendLine("\nSummary:")
                    appendLine(note.summary)
                }
            }
        }
        
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, if (notes.size == 1) notes[0].first.title ?: "Shared Note" else "Shared Notes")
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
    }
    
    fun sharePdf(pdfUri: Uri): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, pdfUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    
    private fun formatTimestamp(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }
} 