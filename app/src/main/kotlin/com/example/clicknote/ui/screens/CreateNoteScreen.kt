package com.example.clicknote.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.ui.components.EnhancedWaveformVisualizer
import com.example.clicknote.ui.viewmodel.CreateNoteViewModel
import com.example.clicknote.domain.model.TranscriptionState
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateNoteScreen(
    onNavigateBack: () -> Unit,
    viewModel: CreateNoteViewModel = hiltViewModel()
) {
    val isRecording by viewModel.isRecording.collectAsState()
    val amplitudes by viewModel.audioAmplitudes.collectAsState()
    val transcriptionState by viewModel.transcriptionState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val titleState = remember { mutableStateOf(TextFieldValue()) }
    val contentState = remember { mutableStateOf(TextFieldValue()) }

    LaunchedEffect(transcriptionState) {
        when (transcriptionState) {
            is TranscriptionState.Completed -> {
                contentState.value = TextFieldValue((transcriptionState as TranscriptionState.Completed).text)
            }
            is TranscriptionState.Error -> {
                snackbarHostState.showSnackbar(
                    message = (transcriptionState as TranscriptionState.Error).error.message ?: "Unknown error",
                    duration = SnackbarDuration.Short
                )
            }
            else -> {}
        }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is CreateNoteEvent.NavigateBack -> onNavigateBack()
                is CreateNoteEvent.ShowError -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
                is CreateNoteEvent.ShowSuccess -> {
                    snackbarHostState.showSnackbar(
                        message = event.message,
                        duration = SnackbarDuration.Short
                    )
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Note") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Navigate back")
                    }
                },
                actions = {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .size(24.dp)
                                .padding(end = 16.dp)
                        )
                    } else {
                        IconButton(
                            onClick = {
                                viewModel.saveNote(
                                    title = titleState.value.text,
                                    content = contentState.value.text
                                )
                            }
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save note")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (isRecording) {
                        viewModel.stopRecording()
                    } else {
                        viewModel.startRecording()
                    }
                },
                containerColor = if (isRecording) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                }
            ) {
                Icon(
                    imageVector = if (isRecording) {
                        Icons.Default.Stop
                    } else {
                        Icons.Default.Mic
                    },
                    contentDescription = if (isRecording) {
                        "Stop recording"
                    } else {
                        "Start recording"
                    }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = titleState.value,
                onValueChange = { titleState.value = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving
            )

            if (isRecording || transcriptionState is TranscriptionState.Processing) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRecording) {
                        EnhancedWaveformVisualizer(
                            amplitudes = amplitudes,
                            isRecording = isRecording,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    if (transcriptionState is TranscriptionState.Processing) {
                        CircularProgressIndicator()
                    }
                }
            }

            OutlinedTextField(
                value = contentState.value,
                onValueChange = { contentState.value = it },
                label = { Text("Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                textStyle = MaterialTheme.typography.bodyLarge,
                enabled = !isSaving && transcriptionState !is TranscriptionState.Processing
            )
        }
    }
} 