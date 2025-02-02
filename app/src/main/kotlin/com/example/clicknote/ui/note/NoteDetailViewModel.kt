package com.example.clicknote.ui.note

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.repository.NoteRepository
import com.example.clicknote.domain.service.AIService
import com.example.clicknote.service.AudioPlayer
import com.example.clicknote.service.ClipboardService
import com.example.clicknote.service.SummaryService
import com.example.clicknote.service.SummaryOptions
import com.example.clicknote.data.repository.NotesRepository
import com.example.clicknote.ui.components.DateRange
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.LocalDate
import javax.inject.Inject

data class NoteDetailUiState(
    val note: Note? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPlaying: Boolean = false,
    val audioProgress: Float = 0f,
    val audioDuration: String = "0:00",
    val currentTab: NoteDetailTab = NoteDetailTab.TRANSCRIPTION,
    val searchQuery: String = "",
    val timeFilter: TimeFilter = TimeFilter.ALL,
    val isGeneratingSummary: Boolean = false,
    val summaryError: String? = null,
    val showSearchBar: Boolean = false,
    val showSummary: Boolean = false,
    val summary: String? = null,
    val keyPoints: List<String> = emptyList(),
    val searchResults: List<String> = emptyList(),
    val summaryState: SummaryState = SummaryState.IDLE,
    val summaryProgress: Float = 0f,
    val playbackState: PlaybackState = PlaybackState.STOPPED,
    val currentPosition: Long = 0,
    val duration: Long = 0,
    val playbackSpeed: Float = 1.0f,
    val isLooping: Boolean = false,
    val message: String? = null,
    val showingSummary: Boolean = false,
    val selectedDateRange: DateRange = DateRange.All,
    val filteredSegments: List<TranscriptionSegment> = emptyList(),
    val hasAudio: Boolean = false
)

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val noteRepository: NoteRepository,
    private val audioPlayer: AudioPlayer,
    private val summaryService: SummaryService,
    private val clipboardService: ClipboardService,
    private val aiService: AIService,
    savedStateHandle: SavedStateHandle,
    private val notesRepository: NotesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(NoteDetailUiState())
    val uiState: StateFlow<NoteDetailUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currentTab = MutableStateFlow(NoteTab.TRANSCRIPTION)
    val currentTab: StateFlow<NoteTab> = _currentTab.asStateFlow()

    private val noteId: String = checkNotNull(savedStateHandle["noteId"]) {
        "Note ID is required"
    }

    private var mediaPlayer: MediaPlayer? = null
    private var playbackJob: kotlinx.coroutines.Job? = null
    private var isLooping = false
    private val skipDuration = 10_000L // 10 seconds in milliseconds

    init {
        loadNote()
        observeAudioState()
        viewModelScope.launch {
            summaryService.summaryState.collect { state ->
                _uiState.update { it.copy(summaryState = state) }
            }
        }

        viewModelScope.launch {
            summaryService.progress.collect { progress ->
                _uiState.update { it.copy(summaryProgress = progress) }
            }
        }
    }

    private fun loadNote() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val note = notesRepository.getNoteById(noteId)
                _uiState.update { state ->
                    state.copy(
                        note = note,
                        isLoading = false,
                        error = null,
                        hasAudio = note.audioPath != null,
                        duration = note.duration ?: 0L,
                        filteredSegments = note.segments
                    )
                }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load note"
                ) }
            }
        }
    }

    private fun initializeMediaPlayer(audioPath: String) {
        mediaPlayer?.release()
        mediaPlayer = MediaPlayer().apply {
            try {
                setDataSource(audioPath)
                prepare()
                _uiState.update { 
                    it.copy(
                        playbackState = PlaybackState.STOPPED,
                        currentPosition = 0,
                        duration = duration.toLong()
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.update { 
                    it.copy(error = "Failed to load audio file")
                }
            }
        }
    }

    private fun observeAudioState() {
        viewModelScope.launch {
            audioPlayer.isPlaying
                .collect { isPlaying ->
                    _uiState.update { it.copy(isPlaying = isPlaying) }
                }
        }
        
        viewModelScope.launch {
            audioPlayer.progress
                .collect { progress ->
                    _uiState.update { it.copy(audioProgress = progress) }
                }
        }
        
        viewModelScope.launch {
            audioPlayer.duration
                .collect { duration ->
                    _uiState.update { it.copy(audioDuration = formatDuration(duration)) }
                }
        }
    }

    fun playAudio() {
        mediaPlayer?.let { player ->
            player.start()
            _uiState.update { it.copy(playbackState = PlaybackState.PLAYING) }
            startPlaybackProgressTracking()
        }
    }

    fun pauseAudio() {
        mediaPlayer?.let { player ->
            player.pause()
            _uiState.update { it.copy(playbackState = PlaybackState.PAUSED) }
            playbackJob?.cancel()
        }
    }

    fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
        _uiState.update { it.copy(currentPosition = position) }
    }

    fun setPlaybackSpeed(speed: Float) {
        mediaPlayer?.let { player ->
            player.playbackParams = player.playbackParams.setSpeed(speed)
            _uiState.update { it.copy(playbackSpeed = speed) }
        }
    }

    fun skipForward() {
        mediaPlayer?.let { player ->
            val newPosition = (player.currentPosition + skipDuration)
                .coerceAtMost(player.duration.toLong())
            seekTo(newPosition)
        }
    }

    fun skipBackward() {
        mediaPlayer?.let { player ->
            val newPosition = (player.currentPosition - skipDuration)
                .coerceAtLeast(0L)
            seekTo(newPosition)
        }
    }

    fun toggleLoop() {
        isLooping = !isLooping
        mediaPlayer?.isLooping = isLooping
        _uiState.update { it.copy(isLooping = isLooping) }
    }

    fun togglePin() {
        val note = uiState.value.note ?: return
        viewModelScope.launch {
            try {
                noteRepository.updateNote(note.copy(
                    isPinned = !note.isPinned,
                    timestamp = LocalDateTime.now()
                ))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun moveNoteToTrash() {
        viewModelScope.launch {
            try {
                noteRepository.moveToTrash(listOf(noteId))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun copyNoteToClipboard() {
        uiState.value.note?.let { note ->
            clipboardService.copyToClipboard(note.content)
        }
    }

    fun searchInNote(query: String) {
        viewModelScope.launch {
            _searchQuery.value = query
            if (query.isBlank()) {
                _uiState.update { it.copy(searchResults = emptyList()) }
                return@launch
            }
            
            uiState.value.note?.let { note ->
                val results = note.content
                    .split("\n")
                    .filter { it.contains(query, ignoreCase = true) }
                _uiState.update { it.copy(searchResults = results) }
            }
        }
    }

    fun toggleSummaryView() {
        _uiState.update { it.copy(showSummary = !it.showSummary) }
        if (_uiState.value.showSummary && _uiState.value.summary == null) {
            generateSummary()
        }
    }

    fun generateSummary() {
        val note = uiState.value.note ?: return
        
        viewModelScope.launch {
            _uiState.update { it.copy(isGeneratingSummary = true, summaryError = null) }
            try {
                // Try Claude first for better summarization
                val summaryResult = claudeService.generateSummary(
                    text = note.content,
                    options = ClaudeSummaryOptions(
                        maxLength = 500,
                        style = ClaudeStyle.PROFESSIONAL,
                        format = ContentFormat.PARAGRAPH
                    )
                ).getOrElse {
                    // Fallback to OpenAI if Claude fails
                    openAiService.generateSummary(
                        text = note.content,
                        options = SummaryOptions(
                            maxLength = 500,
                            style = TextStyle.PROFESSIONAL,
                            format = OutputFormat.PARAGRAPH
                        )
                    ).getOrThrow()
                }

                // Extract key points using Claude's action item extraction
                val keyPoints = claudeService.extractActionItems(note.content).getOrElse {
                    // Fallback to OpenAI for key points
                    openAiService.extractKeyPoints(note.content).getOrThrow()
                }

                // Update the note with summary and key points
                val updatedNote = note.copy(
                    summary = summaryResult,
                    keyPoints = keyPoints,
                    lastModified = LocalDateTime.now()
                )
                noteRepository.updateNote(updatedNote)
                
                _uiState.update { it.copy(
                    note = updatedNote,
                    isGeneratingSummary = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    isGeneratingSummary = false,
                    summaryError = e.message ?: "Failed to generate summary"
                ) }
            }
        }
    }

    fun switchTab(tab: NoteTab) {
        _currentTab.value = tab
        if (tab == NoteTab.SUMMARY && _uiState.value.summary == null) {
            generateSummary()
        }
    }

    fun updateNote(content: String) {
        val note = uiState.value.note ?: return
        viewModelScope.launch {
            try {
                noteRepository.updateNote(note.copy(
                    content = content,
                    timestamp = LocalDateTime.now()
                ))
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    private fun formatDuration(milliseconds: Long): String {
        val seconds = milliseconds / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return "%d:%02d".format(minutes, remainingSeconds)
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
        playbackJob?.cancel()
        viewModelScope.launch {
            audioPlayer.release()
        }
    }

    fun updateTab(tab: NoteDetailTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun clearSearch() {
        _uiState.update { it.copy(searchQuery = "") }
    }

    fun updateTimeFilter(filter: TimeFilter) {
        _uiState.update { it.copy(timeFilter = filter) }
    }

    fun shareNote() {
        val note = uiState.value.note ?: return
        val shareText = buildShareText(note)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Note")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun showMoreOptions() {
        // This will be handled by the UI layer
        // The UI will show a dropdown menu or bottom sheet with additional options
    }

    private fun buildShareText(note: Note): String {
        return buildString {
            append(note.content)
            note.summary?.let { summary ->
                append("\n\nSummary:\n")
                append(summary)
            }
        }
    }

    fun exportAudio() {
        viewModelScope.launch {
            val note = uiState.value.note ?: return@launch
            try {
                val outputFile = createExportFile(note)
                copyAudioFile(note.audioPath!!, outputFile.absolutePath)
                _uiState.update { 
                    it.copy(message = "Audio exported successfully")
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(error = "Failed to export audio: ${e.message}")
                }
            }
        }
    }

    private fun createExportFile(note: Note): File {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))
        val fileName = "ClickNote_${note.id}_$timestamp.m4a"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        return File(downloadsDir, fileName)
    }

    private fun copyAudioFile(sourcePath: String, destPath: String) {
        File(sourcePath).inputStream().use { input ->
            File(destPath).outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }

    private fun startPlaybackProgressTracking() {
        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            while (true) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _uiState.update { 
                            it.copy(currentPosition = player.currentPosition.toLong())
                        }
                        if (player.currentPosition >= player.duration && !isLooping) {
                            _uiState.update { 
                                it.copy(
                                    playbackState = PlaybackState.STOPPED,
                                    currentPosition = 0
                                )
                            }
                            player.seekTo(0)
                            break
                        }
                    }
                }
                kotlinx.coroutines.delay(50) // Update every 50ms
            }
        }
    }

    fun setShowingSummary(showing: Boolean) {
        _uiState.update { it.copy(showingSummary = showing) }
        if (showing && _uiState.value.summary == null) {
            generateSummary()
        }
    }

    fun setDateRange(range: DateRange) {
        _uiState.update { it.copy(selectedDateRange = range) }
        applyFilters()
    }

    private fun applyFilters() {
        val note = _uiState.value.note ?: return
        val query = _uiState.value.searchQuery
        val dateRange = _uiState.value.selectedDateRange.getDateRange()
        
        val filteredSegments = note.segments.filter { segment ->
            val matchesQuery = query.isEmpty() || 
                segment.text.contains(query, ignoreCase = true)
            
            val matchesDate = if (dateRange != null) {
                val segmentDate = LocalDate.ofEpochDay(segment.timestamp / (24 * 60 * 60 * 1000))
                !segmentDate.isBefore(dateRange.first) && !segmentDate.isAfter(dateRange.second)
            } else true
            
            matchesQuery && matchesDate
        }
        
        _uiState.update { it.copy(filteredSegments = filteredSegments) }
    }

    fun togglePlayback() {
        val isPlaying = _uiState.value.isPlaying
        if (isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    private fun startPlayback() {
        viewModelScope.launch {
            try {
                notesRepository.startPlayback(
                    noteId,
                    _uiState.value.currentPosition,
                    _uiState.value.playbackSpeed
                )
                _uiState.update { it.copy(isPlaying = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to start playback: ${e.message}"
                ) }
            }
        }
    }

    private fun pausePlayback() {
        viewModelScope.launch {
            try {
                notesRepository.pausePlayback()
                _uiState.update { it.copy(isPlaying = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = "Failed to pause playback: ${e.message}"
                ) }
            }
        }
    }

    fun seekToTimestamp(timestamp: Long) {
        seekTo(timestamp)
    }

    companion object {
        private const val NOTE_ID_KEY = "noteId"
    }
}

enum class NoteTab {
    TRANSCRIPTION,
    SUMMARY
} 