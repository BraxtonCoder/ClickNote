package com.example.clicknote.ui.recording

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.R
import com.example.clicknote.service.InternalAudioCaptureManager

@Composable
fun InternalAudioCaptureScreen(
    viewModel: InternalAudioCaptureViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(uiState.isRequestingPermission) {
        if (uiState.isRequestingPermission) {
            (context as? Activity)?.let { activity ->
                InternalAudioCaptureManager(activity.applicationContext)
                    .requestMediaProjectionPermission(activity)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (uiState.isRecording) {
                stringResource(R.string.recording_internal_audio)
            } else {
                stringResource(R.string.tap_to_start_recording)
            },
            style = MaterialTheme.typography.titleMedium
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        FloatingActionButton(
            onClick = {
                if (uiState.isRecording) {
                    viewModel.stopRecording()
                } else {
                    viewModel.startRecording()
                }
            }
        ) {
            Icon(
                imageVector = if (uiState.isRecording) {
                    Icons.Default.Stop
                } else {
                    Icons.Default.Mic
                },
                contentDescription = if (uiState.isRecording) {
                    stringResource(R.string.stop_recording)
                } else {
                    stringResource(R.string.start_recording)
                }
            )
        }
        
        uiState.error?.let { error ->
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
} 