package com.example.clicknote.ui.components

import android.content.Intent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextSnippet
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.clicknote.R
import com.example.clicknote.domain.model.ExportSettings
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.TranscriptionSegment
import com.example.clicknote.util.NoteExporter
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareMenu(
    notes: List<Pair<Note, List<TranscriptionSegment>>>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val noteExporter = remember { NoteExporter(context) }
    var isExporting by remember { mutableStateOf(false) }
    var showPreview by remember { mutableStateOf(false) }
    var exportSettings by remember { mutableStateOf(ExportSettings()) }
    
    if (showPreview) {
        ExportPreviewDialog(
            notes = notes,
            settings = exportSettings,
            onSettingsChange = { exportSettings = it },
            onConfirm = {
                isExporting = true
                scope.launch {
                    try {
                        val pdfUri = noteExporter.exportAsPdf(notes, exportSettings)
                        val intent = noteExporter.sharePdf(pdfUri)
                        context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)))
                    } finally {
                        isExporting = false
                        showPreview = false
                        onDismiss()
                    }
                }
            },
            onDismiss = { showPreview = false }
        )
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(
                    if (notes.size == 1) R.string.export_single_note
                    else R.string.export_multiple_notes,
                    notes.size
                ),
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            // Share as Text
            OutlinedButton(
                onClick = {
                    val intent = noteExporter.shareAsText(notes, exportSettings)
                    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share_via)))
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.TextSnippet,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.share_as_text))
            }
            
            // Export as PDF
            Button(
                onClick = { showPreview = true },
                enabled = !isExporting,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                if (isExporting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isExporting) {
                        stringResource(R.string.exporting_pdf)
                    } else {
                        stringResource(R.string.share_as_pdf)
                    }
                )
            }
        }
    }
} 