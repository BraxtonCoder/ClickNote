package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.service.SummaryService
import com.example.clicknote.domain.service.Summary
import com.example.clicknote.domain.service.SummaryTemplate
import com.example.clicknote.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Singleton
import java.util.UUID

@Singleton
class SummaryServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore,
    private val openAiService: OpenAiService,
    private val claudeService: ClaudeService,
    @ApplicationScope private val coroutineScope: CoroutineScope
) : SummaryService {

    override val id: String = "summary_service"
    private var initialized = false
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var currentJob: Job? = null
    private val _summaryProgress = MutableStateFlow(0f)
    private var _isSummarizing = MutableStateFlow(false)

    init {
        initialized = true
    }

    override fun cleanup() {
        scope.cancel()
        initialized = false
    }

    override fun isInitialized(): Boolean = initialized

    override suspend fun generateSummary(text: String): Result<Summary> = runCatching {
        _isSummarizing.value = true
        _summaryProgress.value = 0f

        try {
            // First try with Claude, fallback to OpenAI if it fails
            val summaryText = claudeService.generateSummary(text).getOrElse {
                openAiService.generateSummary(text).getOrThrow()
            }

            _summaryProgress.value = 0.5f

            // Extract key points
            val keyPoints = claudeService.extractActionItems(text).getOrElse {
                openAiService.extractKeyPoints(text).getOrThrow()
            }

            _summaryProgress.value = 0.7f

            // Analyze content for topics and sentiment
            val analysis = claudeService.analyzeContent(text).getOrElse {
                Result.success(ContentAnalysis(
                    topics = emptyList(),
                    sentiment = "neutral",
                    complexity = 0.5f,
                    keyTerms = emptyList(),
                    structure = ContentStructure(
                        sections = emptyList(),
                        hierarchy = emptyMap(),
                        wordCount = text.split("\\s+".toRegex()).size,
                        readingTime = text.split("\\s+".toRegex()).size / 200 // Assuming 200 words per minute
                    )
                )).getOrThrow()
            }

            _summaryProgress.value = 1f

            Summary(
                text = summaryText,
                keyPoints = keyPoints,
                topics = analysis.topics,
                sentiment = analysis.sentiment,
                wordCount = analysis.structure.wordCount,
                readingTime = analysis.structure.readingTime
            )
        } finally {
            _isSummarizing.value = false
        }
    }

    override suspend fun generateSummaryWithTemplate(
        text: String,
        template: SummaryTemplate
    ): Result<Summary> = runCatching {
        _isSummarizing.value = true
        _summaryProgress.value = 0f

        try {
            val options = ClaudeSummaryOptions(
                maxLength = 500,
                style = ClaudeStyle.PROFESSIONAL,
                format = ContentFormat.PARAGRAPH,
                language = "en"
            )

            val prompt = when (template) {
                SummaryTemplate.GENERAL -> "Please provide a general summary of the following text:"
                SummaryTemplate.BUSINESS -> "Please provide a business-focused summary of the following text:"
                SummaryTemplate.ACADEMIC -> "Please provide an academic summary of the following text:"
                SummaryTemplate.TECHNICAL -> "Please provide a technical summary of the following text:"
                SummaryTemplate.MEETING -> "Please provide a meeting summary of the following text:"
                SummaryTemplate.CONVERSATION -> "Please provide a conversation summary of the following text:"
            }

            val summaryText = claudeService.generateSummary(
                text = "$prompt\n\n$text",
                options = options
            ).getOrThrow()

            _summaryProgress.value = 0.5f

            val keyPoints = claudeService.extractActionItems(text).getOrThrow()
            _summaryProgress.value = 0.7f

            val analysis = claudeService.analyzeContent(text).getOrThrow()
            _summaryProgress.value = 1f

            Summary(
                text = summaryText,
                keyPoints = keyPoints,
                topics = analysis.topics,
                sentiment = analysis.sentiment,
                wordCount = analysis.structure.wordCount,
                readingTime = analysis.structure.readingTime
            )
        } finally {
            _isSummarizing.value = false
        }
    }

    override fun getSummaryProgress(): Flow<Float> = _summaryProgress.asStateFlow()

    override fun cancelSummaryGeneration() {
        claudeService.cancelOperation()
        openAiService.cancelOperation()
        _isSummarizing.value = false
        _summaryProgress.value = 0f
    }

    override fun isSummarizing(): Boolean = _isSummarizing.value

    override suspend fun getAvailableTemplates(): Result<List<SummaryTemplate>> = Result.success(
        listOf(
            SummaryTemplate.GENERAL,
            SummaryTemplate.BUSINESS,
            SummaryTemplate.ACADEMIC,
            SummaryTemplate.TECHNICAL,
            SummaryTemplate.MEETING,
            SummaryTemplate.CONVERSATION
        )
    )
} 