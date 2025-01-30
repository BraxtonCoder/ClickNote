package com.example.clicknote.ui.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import com.example.clicknote.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Composable
fun FileSelectionDialog(
    onDismiss: () -> Unit,
    onFileSelected: (Uri) -> Unit,
    isExport: Boolean,
    fileType: String
) {
    val context = LocalContext.current
    var showError by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = if (isExport) {
            ActivityResultContracts.CreateDocument(fileType)
        } else {
            ActivityResultContracts.GetContent()
        }
    ) { uri: Uri? ->
        uri?.let { onFileSelected(it) }
        onDismiss()
    }

    LaunchedEffect(Unit) {
        if (isExport) {
            val timestamp = System.currentTimeMillis()
            launcher.launch("clicknote_backup_$timestamp.zip")
        } else {
            launcher.launch(fileType)
        }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card {
            AlertDialog(
                onDismissRequest = onDismiss,
                title = {
                    Text(
                        text = stringResource(
                            if (isExport) R.string.export_data 
                            else R.string.import_data
                        )
                    )
                },
                text = {
                    Text(
                        text = stringResource(
                            if (isExport) R.string.export_data_description
                            else R.string.import_data_description
                        )
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (isExport) {
                                val timestamp = LocalDateTime.now().format(
                                    DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")
                                )
                                launcher.launch("clicknote_backup_$timestamp.zip")
                            } else {
                                launcher.launch(fileType)
                            }
                        }
                    ) {
                        Text(
                            text = stringResource(
                                if (isExport) R.string.select_export_location
                                else R.string.select_import_file
                            )
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    if (showError) {
        AlertDialog(
            onDismissRequest = { showError = false },
            title = { Text(stringResource(R.string.error)) },
            text = { 
                Text(
                    stringResource(
                        if (isExport) R.string.export_error
                        else R.string.import_error
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = { showError = false }) {
                    Text(stringResource(R.string.ok))
                }
            }
        )
    }
} 