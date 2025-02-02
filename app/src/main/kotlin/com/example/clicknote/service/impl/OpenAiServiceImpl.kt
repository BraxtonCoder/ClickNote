package com.example.clicknote.service.impl

import android.content.Context
import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.domain.model.*
import com.example.clicknote.domain.repository.PreferencesRepository
import com.example.clicknote.service.OpenAiService
import com.example.clicknote.service.api.OpenAiApi
import com.example.clicknote.service.model.*
import com.example.clicknote.domain.service.PerformanceMonitor
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import okio.source
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONObject
import com.example.clicknote.data.model.TranscriptionResult

@Singleton
class OpenAiServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore,
    private val openAiApi: OpenAiApi,
    private val preferencesRepository: PreferencesRepository,
    private val performanceMonitor: Lazy<PerformanceMonitor>,
    private val okHttpClient: OkHttpClient
) : OpenAiService {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private var openAI: OpenAI? = null
    private val _progress = MutableStateFlow(0f)
    override val progress: Flow<Float> = _progress.asStateFlow()
    
    private val _operationInProgress = MutableStateFlow(false)
    override val operationInProgress: Flow<Boolean> = _operationInProgress.asStateFlow()

    companion object {
        private const val WHISPER_API_URL = "https://api.openai.com/v1/audio/transcriptions"
        private const val GPT_API_URL = "https://api.openai.com/v1/chat/completions"
        private const val CHUNK_SIZE = 25 * 1024 * 1024 // 25MB chunks for streaming
        private const val MODEL_GPT4 = "gpt-4-turbo-preview"
        private const val MODEL_WHISPER = "whisper-1"
    }

    init {
        scope.launch {
            initializeOpenAI()
        }
    }

    private suspend fun initializeOpenAI() {
        try {
            val apiKey = preferencesRepository.getOpenAIApiKey()
            if (apiKey.isNotEmpty()) {
                openAI = OpenAI(apiKey)
                _operationInProgress.value = false
            }
        } catch (e: Exception) {
            performanceMonitor.get().trackError(e)
            _operationInProgress.value = false
        }
    }

    override suspend fun transcribeAudio(audioFile: File): String {
        performanceMonitor.get().trackFileTranscription(audioFile)
        val api = openAI ?: throw IllegalStateException("OpenAI not initialized")
        
        return try {
            val request = TranscriptionRequest(
                audio = FileSource(audioFile.name, audioFile.source()),
                model = "whisper-1"
            )
            api.transcription(request).text
        } catch (e: Exception) {
            performanceMonitor.get().trackError(e)
            throw e
        }
    }

    override suspend fun transcribeWithTimestamps(audioFile: File): TranscriptionResult {
        performanceMonitor.get().trackFileTranscription(audioFile)
        val api = openAI ?: throw IllegalStateException("OpenAI not initialized")
        
        return try {
            val request = TranscriptionRequest(
                audio = FileSource(audioFile.name, audioFile.source()),
                model = "whisper-1",
                timestamp_granularities = listOf("word", "segment")
            )
            val response = api.transcription(request)
            // Convert response to TranscriptionResult
            TranscriptionResult("") // Placeholder
        } catch (e: Exception) {
            performanceMonitor.get().trackError(e)
            throw e
        }
    }

    override suspend fun transcribe(audioFile: File): String {
        val apiKey = userPreferences.getOpenAiApiKey() ?: throw IllegalStateException("OpenAI API key not found")
        return openAiApi.transcribeAudio(apiKey, audioFile)
    }

    override suspend fun transcribeStream(audioStream: Flow<ByteArray>): Flow<String> = flow {
        var currentChunk = ByteArray(0)
        audioStream.collect { data ->
            currentChunk += data
            if (currentChunk.size >= CHUNK_SIZE) {
                val tempFile = createTempFile(currentChunk)
                val transcription = transcribeAudio(tempFile).getOrThrow()
                emit(transcription)
                tempFile.delete()
                currentChunk = ByteArray(0)
            }
        }
        
        if (currentChunk.isNotEmpty()) {
            val tempFile = createTempFile(currentChunk)
            val transcription = transcribeAudio(tempFile).getOrThrow()
            emit(transcription)
            tempFile.delete()
        }
    }

    override suspend fun detectSpeakers(audioFile: File): List<String> {
        performanceMonitor.get().startMonitoring("speaker_detection")
        val api = openAI ?: throw IllegalStateException("OpenAI not initialized")
        
        return try {
            val prompt = """
                Analyze the following transcription and identify distinct speakers.
                Label them as "Person 1", "Person 2", etc.
                Return only the speaker labels, one per line.
            """.trimIndent()

            val transcription = transcribeAudio(audioFile).getOrThrow()
            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    ),
                    ChatMessage(
                        role = ChatRole.User,
                        content = transcription
                    )
                )
            )

            val response = api.chat.completions(request).choices.first().message?.content
                ?: throw IllegalStateException("OpenAI client not initialized")

            response.split("\n").filter { it.startsWith("Person") }
        } catch (e: Exception) {
            performanceMonitor.get().trackError(e)
            throw e
        } finally {
            performanceMonitor.get().stopMonitoring("speaker_detection")
        }
    }

    override suspend fun summarize(text: String): String {
        if (openAI == null) {
            initializeOpenAI()
        }

        val prompt = """
            Please provide a concise summary of the following text, highlighting the key points and main ideas:
            
            $text
            
            Format the summary with bullet points for key topics and maintain the original context and meaning.
        """.trimIndent()

        val request = ChatCompletionRequest(
            model = ModelId(MODEL_GPT4),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = prompt
                )
            )
        )

        return openAI?.chat?.completions(request)?.choices?.first()?.message?.content
            ?: throw IllegalStateException("OpenAI client not initialized")
    }

    override suspend fun generateSummary(text: String, options: SummaryOptions): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (openAI == null) {
                initializeOpenAI()
            }

            _operationInProgress.value = true
            currentOperation = "summary"
            _progress.value = 0f

            val prompt = buildString {
                append("Please provide a")
                when (options.style) {
                    TextStyle.CONCISE -> append(" concise")
                    TextStyle.DETAILED -> append(" detailed")
                    TextStyle.TECHNICAL -> append(" technical")
                    TextStyle.CASUAL -> append(" casual")
                    TextStyle.PROFESSIONAL -> append(" professional")
                    TextStyle.CREATIVE -> append(" creative")
                }
                append(" summary of the following text")
                if (options.maxLength > 0) {
                    append(" in ${options.maxLength} words or less")
                }
                append(".\n\nFormat the output as a")
                when (options.format) {
                    OutputFormat.PARAGRAPH -> append(" continuous paragraph")
                    OutputFormat.BULLET_POINTS -> append("n itemized list with bullet points")
                    OutputFormat.NUMBERED_LIST -> append(" numbered list")
                    OutputFormat.OUTLINE -> append("n outline with hierarchical structure")
                    OutputFormat.MARKDOWN -> append(" markdown document")
                }
                append(":\n\n")
                append(text)
            }

            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    )
                )
            )

            val result = openAI?.chat?.completions(request)?.choices?.first()?.message?.content
                ?: throw IllegalStateException("OpenAI client not initialized")
            
            _progress.value = 1f
            _operationInProgress.value = false
            currentOperation = null
            
            result
        }
    }

    override fun streamSummary(text: String, template: String?): Flow<String> = flow {
        try {
            if (openAI == null) {
                initializeOpenAI()
            }

            _operationInProgress.value = true
            currentOperation = "stream_summary"

            val prompt = buildString {
                append("Summarize the following text")
                if (template != null) {
                    append(" using the $template template")
                }
                append(":\n\n")
                append(text)
            }

            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    )
                )
            )

            val response = openAI?.chat?.completions(request)?.choices?.first()?.message?.content
                ?: throw IllegalStateException("OpenAI client not initialized")

            emit(response)
            
            _operationInProgress.value = false
            currentOperation = null
        } catch (e: Exception) {
            _operationInProgress.value = false
            currentOperation = null
            throw e
        }
    }

    override suspend fun askQuestion(text: String, question: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (openAI == null) {
                initializeOpenAI()
            }

            val prompt = """
                Context: $text
                
                Question: $question
                
                Please provide a clear and concise answer based on the context above.
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    )
                )
            )

            openAI?.chat?.completions(request)?.choices?.first()?.message?.content
                ?: throw IllegalStateException("OpenAI client not initialized")
        }
    }

    override suspend fun extractKeyPoints(text: String): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            if (openAI == null) {
                initializeOpenAI()
            }

            val prompt = """
                Extract the key points from the following text. Return each point on a new line, starting with a bullet point (•):
                
                $text
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    )
                )
            )

            val response = openAI?.chat?.completions(request)?.choices?.first()?.message?.content
                ?: throw IllegalStateException("OpenAI client not initialized")

            response.split("\n").filter { it.startsWith("•") }.map { it.substring(2) }
        }
    }

    override fun isAvailable(): Boolean = openAI != null

    override suspend fun cleanup() {
        scope.cancel()
        openAI = null
        _progress.value = 0f
        _operationInProgress.value = false
        currentOperation = null
    }

    private fun createTempFile(data: ByteArray): File {
        return File.createTempFile("stream", ".wav", context.cacheDir).apply {
            writeBytes(data)
        }
    }

    override suspend fun generateTags(text: String): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            if (openAI == null) {
                initializeOpenAI()
            }

            val prompt = """
                Generate relevant tags for the following text. Return only the tags, one per line, without any symbols or prefixes:
                
                $text
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    )
                )
            )

            val response = openAI?.chat?.completions(request)?.choices?.first()?.message?.content
                ?: throw IllegalStateException("OpenAI client not initialized")

            response.split("\n").filter { it.isNotBlank() }
        }
    }

    override suspend fun categorizeContent(text: String): Result<List<String>> = withContext(Dispatchers.IO) {
        runCatching {
            if (openAI == null) {
                initializeOpenAI()
            }

            val prompt = """
                Analyze the following text and identify its main categories or themes. Return each category on a new line:
                
                $text
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    )
                )
            )

            val response = openAI?.chat?.completions(request)?.choices?.first()?.message?.content
                ?: throw IllegalStateException("OpenAI client not initialized")

            response.split("\n").filter { it.isNotBlank() }
        }
    }

    override suspend fun analyzeEmotion(text: String): Result<EmotionAnalysis> = withContext(Dispatchers.IO) {
        runCatching {
            if (openAI == null) {
                initializeOpenAI()
            }

            val prompt = """
                Analyze the emotional content of the following text. Return the results in this format:
                Primary: [primary emotion]
                Confidence: [confidence score between 0 and 1]
                Emotions:
                [emotion1]: [score]
                [emotion2]: [score]
                etc.
                
                Text to analyze:
                $text
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    )
                )
            )

            val response = openAI?.chat?.completions(request)?.choices?.first()?.message?.content
                ?: throw IllegalStateException("OpenAI client not initialized")

            val lines = response.split("\n")
            val primaryEmotion = lines.find { it.startsWith("Primary:") }?.substringAfter(":")?.trim() ?: "neutral"
            val confidence = lines.find { it.startsWith("Confidence:") }?.substringAfter(":")?.trim()?.toFloatOrNull() ?: 0.5f
            
            val emotions = lines.dropWhile { !it.startsWith("Emotions:") }
                .drop(1)
                .filter { it.contains(":") }
                .associate { line ->
                    val (emotion, score) = line.split(":")
                    emotion.trim() to (score.trim().toFloatOrNull() ?: 0f)
                }

            EmotionAnalysis(primaryEmotion, confidence, emotions)
        }
    }

    override suspend fun generateTitle(text: String): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (openAI == null) {
                initializeOpenAI()
            }

            val prompt = """
                Generate a concise and descriptive title for the following text. Return only the title, without any additional text or formatting:
                
                $text
            """.trimIndent()

            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    )
                )
            )

            openAI?.chat?.completions(request)?.choices?.first()?.message?.content?.trim()
                ?: throw IllegalStateException("OpenAI client not initialized")
        }
    }

    override suspend fun enhanceText(text: String, style: TextStyle): Result<String> = withContext(Dispatchers.IO) {
        runCatching {
            if (openAI == null) {
                initializeOpenAI()
            }

            val prompt = buildString {
                append("Enhance the following text to make it more")
                when (style) {
                    TextStyle.CONCISE -> append(" concise and to the point")
                    TextStyle.DETAILED -> append(" detailed and comprehensive")
                    TextStyle.TECHNICAL -> append(" technical and precise")
                    TextStyle.CASUAL -> append(" casual and conversational")
                    TextStyle.PROFESSIONAL -> append(" professional and formal")
                    TextStyle.CREATIVE -> append(" creative and engaging")
                }
                append(", while maintaining its original meaning:\n\n")
                append(text)
            }

            val request = ChatCompletionRequest(
                model = ModelId(MODEL_GPT4),
                messages = listOf(
                    ChatMessage(
                        role = ChatRole.System,
                        content = prompt
                    )
                )
            )

            openAI?.chat?.completions(request)?.choices?.first()?.message?.content
                ?: throw IllegalStateException("OpenAI client not initialized")
        }
    }

    override fun getProgress(): Flow<Float> = _progress.asStateFlow()

    override fun cancelOperation() {
        _operationInProgress.value = false
        currentOperation = null
        _progress.value = 0f
    }

    override suspend fun isOperationInProgress(): Boolean = _operationInProgress.value

    override suspend fun getAvailableModels(): List<OpenAiModel> = withContext(Dispatchers.IO) {
        if (openAI == null) {
            initializeOpenAI()
        }

        listOf(
            OpenAiModel(
                id = MODEL_GPT4,
                name = "GPT-4 Turbo",
                capabilities = listOf(
                    "Text generation",
                    "Summarization",
                    "Analysis",
                    "Question answering"
                ),
                maxTokens = 4096,
                isAvailable = true
            ),
            OpenAiModel(
                id = MODEL_WHISPER,
                name = "Whisper v1",
                capabilities = listOf(
                    "Speech to text",
                    "Audio transcription",
                    "Speaker diarization"
                ),
                maxTokens = 0,
                isAvailable = true
            )
        )
    }
} 