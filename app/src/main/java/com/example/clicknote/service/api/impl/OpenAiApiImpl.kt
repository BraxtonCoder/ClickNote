package com.example.clicknote.service.api.impl

import com.aallam.openai.api.audio.TranscriptionRequest
import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.file.FileSource
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import com.example.clicknote.service.api.OpenAiApi
import com.example.clicknote.service.model.*
import okio.source
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File

@Singleton
class OpenAiApiImpl @Inject constructor(
    private val client: OkHttpClient
) : OpenAiApi {

    companion object {
        private const val GPT_API_URL = "https://api.openai.com/v1/chat/completions"
        private const val WHISPER_API_URL = "https://api.openai.com/v1/audio/transcriptions"
        private const val MODEL = "gpt-4-turbo-preview"
        private const val MAX_TOKENS = 4000
    }

    override suspend fun transcribe(
        apiKey: String,
        request: TranscriptionRequest
    ): TranscriptionResponse {
        val openAI = OpenAI(apiKey)
        
        val transcriptionRequest = TranscriptionRequest(
            audio = FileSource(
                name = request.audioFile.name,
                source = request.audioFile.source()
            ),
            model = ModelId("whisper-1"),
            language = request.language?.code
        )
        
        val result = openAI.audio.transcribe(transcriptionRequest)
        return TranscriptionResponse(
            text = result.text,
            segments = emptyList() // TODO: Parse segments from response
        )
    }

    override suspend fun summarize(
        apiKey: String,
        request: SummaryRequest
    ): SummaryResponse {
        val openAI = OpenAI(apiKey)
        
        val prompt = buildString {
            append("Summarize the following text")
            if (request.maxLength != null) {
                append(" in ${request.maxLength} words or less")
            }
            if (request.template != null) {
                append(" using the ${request.template} template")
            }
            append(":\n\n")
            append(request.text)
            
            if (request.extractKeyPoints) {
                append("\n\nAlso extract key points from the text.")
            }
            if (request.extractEntities) {
                append("\n\nIdentify important entities (${request.entityTypes.joinToString()}).")
            }
        }
        
        val chatRequest = ChatCompletionRequest(
            model = ModelId("gpt-4"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = prompt
                )
            )
        )
        
        val response = openAI.chat.completions(chatRequest).choices.first().message.content
        
        // Parse the response into sections
        val sections = response.split("\n\n")
        return SummaryResponse(
            summary = sections.first(),
            keyPoints = if (request.extractKeyPoints) sections.getOrNull(1)?.lines() ?: emptyList() else emptyList(),
            topics = emptyList(),
            entities = if (request.extractEntities) sections.getOrNull(2)?.lines() ?: emptyList() else emptyList(),
            timeline = emptyList()
        )
    }

    override suspend fun complete(apiKey: String, prompt: String): String {
        val requestBody = JSONObject().apply {
            put("model", MODEL)
            put("messages", listOf(
                JSONObject().apply {
                    put("role", "system")
                    put("content", prompt)
                }
            ))
            put("max_tokens", MAX_TOKENS)
            put("temperature", 0.7)
        }

        val request = Request.Builder()
            .url(GPT_API_URL)
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(requestBody.toString().toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val error = response.body?.string() ?: "Unknown error"
                throw IllegalStateException("API call failed: $error")
            }

            val jsonResponse = JSONObject(response.body!!.string())
            return jsonResponse
                .getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()
        }
    }

    override suspend fun transcribeAudio(apiKey: String, audioFile: File): String {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/wav".toMediaType())
            )
            .addFormDataPart("model", "whisper-1")
            .build()

        val request = Request.Builder()
            .url(WHISPER_API_URL)
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val error = response.body?.string() ?: "Unknown error"
                throw IllegalStateException("API call failed: $error")
            }

            val jsonResponse = JSONObject(response.body!!.string())
            return jsonResponse.getString("text").trim()
        }
    }

    override suspend fun transcribeAudioWithTimestamps(
        apiKey: String, 
        audioFile: File, 
        language: Language?
    ): TranscriptionResult {
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "file",
                audioFile.name,
                audioFile.asRequestBody("audio/wav".toMediaType())
            )
            .addFormDataPart("model", "whisper-1")
            .addFormDataPart("response_format", "verbose_json")
            .addFormDataPart("timestamp_granularities", "segment")
            .apply {
                language?.let { addFormDataPart("language", it.code) }
            }
            .build()

        val request = Request.Builder()
            .url(WHISPER_API_URL)
            .header("Authorization", "Bearer $apiKey")
            .post(requestBody)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                val error = response.body?.string() ?: "Unknown error"
                throw IllegalStateException("API call failed: $error")
            }

            val jsonResponse = JSONObject(response.body!!.string())
            return TranscriptionResult(
                text = jsonResponse.getString("text"),
                segments = emptyList() // TODO: Parse segments from response
            )
        }
    }

    override suspend fun detectSpeakers(apiKey: String, audioFile: File): List<String> {
        val transcription = transcribeAudio(apiKey, audioFile)
        
        val prompt = """
            Analyze the following conversation transcript and identify the distinct speakers.
            Label them as "Person 1", "Person 2", etc. Return only the list of speakers.
            
            Transcript:
            $transcription
        """.trimIndent()

        val speakers = complete(apiKey, prompt)
        return speakers.split("\n").filter { it.startsWith("Person") }
    }

    override suspend fun summarizeText(apiKey: String, text: String): String {
        val prompt = """
            Please provide a concise summary of the following text, highlighting the key points and main ideas:
            
            $text
            
            Format the summary with bullet points for key topics and maintain the original context and meaning.
        """.trimIndent()

        return complete(apiKey, prompt)
    }
} 