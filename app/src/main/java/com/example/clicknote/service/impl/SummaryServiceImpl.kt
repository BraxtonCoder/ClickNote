package com.example.clicknote.service.impl

import android.content.Context
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.*
import com.example.clicknote.service.*
import com.example.clicknote.service.model.SummaryRequest
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
    private val claudeService: ClaudeService
) : SummaryService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var currentJob: Job? = null

    private val _summaryState = MutableStateFlow(SummaryState.IDLE)
    override val summaryState: StateFlow<SummaryState> = _summaryState.asStateFlow()

    private val _progress = MutableStateFlow(0f)
    override val progress: Flow<Float> = _progress.asStateFlow()

    private val _templates = MutableStateFlow<List<SummaryTemplate>>(emptyList())
    override val templates: Flow<List<SummaryTemplate>> = _templates.asStateFlow()

    private val _summaryProgress = MutableStateFlow(0f)
    private var _isSummarizing = MutableStateFlow(false)

    init {
        loadDefaultTemplates()
    }

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

            val summaryText = claudeService.generateSummary(
                text = "${template.prompt}\n\n$text",
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

    override suspend fun generateDetailedSummary(
        text: String,
        options: SummaryOptions
    ): Result<DetailedSummary> = runCatching {
        _summaryState.value = SummaryState.GENERATING
        _progress.value = 0f

        val topics = extractTopics(text).getOrThrow()
        val entities = extractEntities(text).getOrThrow()
        val timeline = extractTimeline(text).getOrThrow()
        val keyPoints = generateKeyPoints(text).getOrThrow()
        val actionItems = generateActionItems(text).getOrThrow()

        _progress.value = 1f
        _summaryState.value = SummaryState.SUCCESS

        DetailedSummary(
            id = UUID.randomUUID().toString(),
            noteId = UUID.randomUUID().toString(),
            summary = Summary(
                id = UUID.randomUUID().toString(),
                noteId = UUID.randomUUID().toString(),
                content = text,
                wordCount = text.split("\\s+".toRegex()).size,
                sourceWordCount = text.split("\\s+".toRegex()).size
            ),
            topics = topics,
            entities = entities,
            timeline = timeline
        )
    }.onFailure {
        _summaryState.value = SummaryState.ERROR
    }

    override suspend fun extractTopics(
        text: String,
        maxTopics: Int
    ): Result<List<Topic>> = runCatching {
        _summaryState.value = SummaryState.GENERATING

        val topics = (1..maxTopics).map { index ->
            Topic(
                id = UUID.randomUUID().toString(),
                name = "Topic $index",
                relevance = 1f / index,
                mentions = 1,
                subtopics = emptyList()
            )
        }

        _summaryState.value = SummaryState.SUCCESS
        topics
    }

    override suspend fun extractEntities(
        text: String,
        types: Set<EntityType>
    ): Result<List<Entity>> = runCatching {
        emptyList()
    }

    override suspend fun extractTimeline(
        text: String,
        maxEvents: Int
    ): Result<List<TimelineEvent>> = runCatching {
        emptyList()
    }

    override suspend fun generateKeyPoints(
        text: String,
        maxPoints: Int
    ): Result<List<String>> = runCatching {
        (1..maxPoints).map { "Key point $it" }
    }

    override suspend fun generateActionItems(
        text: String,
        maxItems: Int
    ): Result<List<String>> = runCatching {
        (1..maxItems).map { "Action item $it" }
    }

    override suspend fun generateTitle(
        text: String,
        maxLength: Int
    ): Result<String> = runCatching {
        text.take(maxLength)
    }

    override suspend fun generateTags(
        text: String,
        maxTags: Int
    ): Result<List<String>> = runCatching {
        (1..maxTags).map { "tag$it" }
    }

    override fun getAvailableTemplates(): List<SummaryTemplate> {
        return defaultTemplates
    }

    override fun getTemplateByName(name: String): SummaryTemplate? {
        return defaultTemplates.find { it.name == name }
    }

    override suspend fun generateStreamingSummary(
        text: String,
        options: SummaryOptions
    ): Flow<String> = flow {
        emit("Streaming summary...")
    }

    override suspend fun cancelSummarization() {
        currentJob?.cancel()
        _summaryState.value = SummaryState.IDLE
        _progress.value = 0f
    }

    override suspend fun getTemplatesForCategory(category: String): List<SummaryTemplate> {
        return getAvailableTemplates().filter { it.category.name == category }
    }

    override suspend fun createTemplate(template: SummaryTemplate) {
        val currentTemplates = _templates.value.toMutableList()
        currentTemplates.add(template)
        _templates.value = currentTemplates
    }

    override suspend fun deleteTemplate(templateId: String) {
        val currentTemplates = _templates.value.toMutableList()
        currentTemplates.removeAll { it.id == templateId }
        _templates.value = currentTemplates
    }

    override fun streamSummary(text: String, templateType: TemplateType?): Flow<String> = flow {
        emit("Streaming summary...")
    }

    override suspend fun askQuestion(text: String, question: String): Result<String> = runCatching {
        "Answer to: $question"
    }

    override fun isAvailable(): Boolean = true

    private fun loadDefaultTemplates() {
        _templates.value = defaultTemplates
    }

    companion object {
        private val defaultTemplates = listOf(
            SummaryTemplate(
                id = "general_brief",
                name = "Brief Overview",
                description = "A concise summary highlighting key points",
                category = TemplateCategory.GENERAL,
                prompt = "Provide a brief overview of the main points discussed in this recording."
            ),
            SummaryTemplate(
                id = "business_meeting",
                name = "Meeting Minutes",
                description = "Structured summary of meeting discussions and action items",
                category = TemplateCategory.BUSINESS,
                prompt = "Summarize this meeting recording in a structured format including: 1. Key Discussion Points 2. Decisions Made 3. Action Items 4. Next Steps"
            ),
            SummaryTemplate(
                id = "academic_lecture",
                name = "Lecture Notes",
                description = "Organized summary of academic lecture content",
                category = TemplateCategory.ACADEMIC,
                prompt = "Create structured lecture notes including: Main Concepts, Key Terms, Examples, and Important Relationships"
            )
        )
    }
} 