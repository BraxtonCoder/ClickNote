package com.example.clicknote.ui.note

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.*
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.clicknote.ui.components.AudioPlayer
import com.example.clicknote.ui.components.SearchBar
import com.example.clicknote.domain.model.Note
import com.example.clicknote.domain.model.TimeFilter
import com.example.clicknote.ui.components.LoadingScreen
import com.example.clicknote.ui.components.ErrorScreen
import com.example.clicknote.ui.components.DateRangePicker
import com.example.clicknote.ui.components.NoteSummary
import com.example.clicknote.ui.components.NoteContent

enum class NoteDetailTab {
    TRANSCRIPTION,
    SUMMARY
}

data class NoteDetailUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentTab: NoteDetailTab = NoteDetailTab.TRANSCRIPTION,
    val searchQuery: String = "",
    val timeFilter: TimeFilter = TimeFilter.ALL,
    val isGeneratingSummary: Boolean = false,
    val currentPosition: Float = 0f,
    val duration: Float = 0f,
    val playbackState: AudioPlayer.PlaybackState = AudioPlayer.PlaybackState.PAUSED,
    val playbackSpeed: Float = 1f,
    val isLooping: Boolean = false,
    val message: String? = null,
    val showingSummary: Boolean = false,
    val summary: String? = null,
    val filteredSegments: List<Segment> = emptyList(),
    val hasAudio: Boolean = false,
    val isPlaying: Boolean = false,
    val selectedDateRange: Pair<LocalDate, LocalDate> = Pair(LocalDate.now(), LocalDate.now()),
    val isEditing: Boolean = false,
    val summaryError: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteDetailScreen(
    noteId: String,
    onNavigateUp: () -> Unit,
    viewModel: NoteDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    AnimatedContent(
                        targetState = uiState.note?.title,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) with 
                            fadeOut(animationSpec = tween(300))
                        }
                    ) { title ->
                        Text(
                            text = title ?: "Note Details",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    AnimatedVisibility(
                        visible = uiState.note != null,
                        enter = fadeIn() + expandHorizontally(),
                        exit = fadeOut() + shrinkHorizontally()
                    ) {
                        Row {
                            IconButton(onClick = { viewModel.togglePin() }) {
                                Icon(
                                    imageVector = if (uiState.note?.isPinned == true) {
                                        Icons.Default.PushPin
                                    } else {
                                        Icons.Outlined.PushPin
                                    },
                                    contentDescription = if (uiState.note?.isPinned == true) {
                                        "Unpin note"
                                    } else {
                                        "Pin note"
                                    }
                                )
                            }
                            IconButton(onClick = { viewModel.shareNote() }) {
                                Icon(Icons.Default.Share, contentDescription = "Share")
                            }
                            IconButton(onClick = { viewModel.showMoreOptions() }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options")
                            }
                        }
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // View mode tabs with animated indicator
            TabRow(
                selectedTabIndex = if (uiState.showingSummary) 1 else 0,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(
                            tabPositions[if (uiState.showingSummary) 1 else 0]
                        )
                    )
                }
            ) {
                Tab(
                    selected = !uiState.showingSummary,
                    onClick = { viewModel.setShowingSummary(false) }
                ) {
                    Text(
                        text = "Transcription",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
                Tab(
                    selected = uiState.showingSummary,
                    onClick = { viewModel.setShowingSummary(true) }
                ) {
                    Text(
                        text = "Summary",
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }
            
            // Animated search bar
            AnimatedVisibility(
                visible = true,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = { viewModel.setSearchQuery(it) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    placeholder = { Text("Search in note") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    trailingIcon = if (uiState.searchQuery.isNotEmpty()) {
                        {
                            IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear search")
                            }
                        }
                    } else null,
                    singleLine = true
                )
            }
            
            // Animated content transition
            AnimatedContent(
                targetState = uiState.showingSummary,
                transitionSpec = {
                    fadeIn(animationSpec = tween(300)) with
                    fadeOut(animationSpec = tween(300))
                }
            ) { showingSummary ->
                if (showingSummary) {
                    NoteSummary(
                        summary = uiState.note?.summary.orEmpty(),
                        keyPoints = uiState.note?.keyPoints.orEmpty(),
                        isLoading = uiState.isGeneratingSummary,
                        error = uiState.summaryError,
                        onRegenerateSummary = viewModel::generateSummary,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    )
                } else {
                    Column {
                        // Date range picker with animation
                        AnimatedVisibility(
                            visible = !uiState.showingSummary,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            DateRangePicker(
                                selectedRange = uiState.selectedDateRange,
                                onRangeSelected = { viewModel.setDateRange(it) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                        
                        NoteContent(
                            content = uiState.note?.content.orEmpty(),
                            searchQuery = uiState.searchQuery,
                            isEditing = uiState.isEditing,
                            onContentChange = viewModel::updateContent,
                            onEditClick = viewModel::toggleEditing,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                        )
                    }
                }
            }
            
            // Audio player with slide-up animation
            AnimatedVisibility(
                visible = uiState.hasAudio,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                AudioPlayer(
                    isPlaying = uiState.isPlaying,
                    currentPosition = uiState.currentPosition,
                    duration = uiState.duration,
                    playbackSpeed = uiState.playbackSpeed,
                    onPlayPause = { viewModel.togglePlayback() },
                    onSeek = { viewModel.seekTo(it) },
                    onSpeedChange = { viewModel.setPlaybackSpeed(it) }
                )
            }
        }
    }
}

@Composable
private fun TranscriptionSegment(
    text: String,
    timestamp: Long,
    isHighlighted: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isHighlighted) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = formatTimestamp(timestamp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = text,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

private fun formatTimestamp(millis: Long): String {
    val duration = Duration.ofMillis(millis)
    val minutes = duration.toMinutes()
    val seconds = duration.minusMinutes(minutes).seconds
    return String.format("%d:%02d", minutes, seconds)
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    timeFilter: TimeFilter,
    onTimeFilterChange: (TimeFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier.weight(1f),
            placeholder = { Text("Search in note") },
            singleLine = true,
            trailingIcon = {
                if (query.isNotEmpty()) {
                    IconButton(onClick = onClearSearch) {
                        Icon(Icons.Default.Clear, "Clear search")
                    }
                }
            }
        )
        IconButton(onClick = { onSearch(query) }) {
            Icon(Icons.Default.Search, "Search")
        }
    }
}

@Composable
private fun TranscriptionContent(
    note: Note,
    searchQuery: String,
    currentPosition: Long,
    onTimestampClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    val activeTimestampColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    SelectionContainer {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            Column {
                note.segments.forEach { segment ->
                    val isActive = currentPosition in segment.startTime..segment.endTime
                    val backgroundColor = if (isActive) activeTimestampColor else Color.Transparent
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(backgroundColor)
                            .clickable { onTimestampClick(segment.startTime) }
                            .padding(vertical = 4.dp)
                    ) {
                        // Timestamp
                        Text(
                            text = formatTimestamp(segment.startTime),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .width(48.dp)
                        )
                        
                        // Content with optional search highlighting
                        if (searchQuery.isNotEmpty()) {
                            val annotatedString = buildAnnotatedString {
                                val content = segment.content
                                var lastIndex = 0
                                val searchRegex = searchQuery.toRegex(RegexOption.IGNORE_CASE)
                                
                                searchRegex.findAll(content).forEach { result ->
                                    // Add text before match
                                    append(content.substring(lastIndex, result.range.first))
                                    
                                    // Add highlighted match
                                    withStyle(
                                        style = SpanStyle(
                                            background = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    ) {
                                        append(result.value)
                                    }
                                    
                                    lastIndex = result.range.last + 1
                                }
                                
                                // Add remaining text
                                if (lastIndex < content.length) {
                                    append(content.substring(lastIndex))
                                }
                            }
                            
                            Text(
                                text = annotatedString,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        } else {
                            Text(
                                text = segment.content,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryContent(
    note: Note,
    isGeneratingSummary: Boolean,
    onGenerateSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        if (isGeneratingSummary) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (note.summary != null) {
            Text(
                text = note.summary,
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "No summary available",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onGenerateSummary
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Summary")
                }
            }
        }
    }
}

@Composable
private fun AudioPlayer(
    isPlaying: Boolean,
    currentPosition: Float,
    duration: Float,
    playbackSpeed: Float,
    onPlayPause: () -> Unit,
    onSeek: (Float) -> Unit,
    onSpeedChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    AudioPlayer(
        isPlaying = isPlaying,
        currentPosition = currentPosition,
        duration = duration,
        playbackSpeed = playbackSpeed,
        onPlayPause = onPlayPause,
        onSeek = onSeek,
        onSpeedChange = onSpeedChange,
        modifier = modifier
    )
} 