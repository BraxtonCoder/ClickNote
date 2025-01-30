package com.example.clicknote.service

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import com.example.clicknote.worker.ModelDownloadWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

data class ModelInfo(
    val name: String,
    val language: String,
    val size: Long,
    val url: String,
    val isDownloaded: Boolean = false
)

sealed class ModelState {
    object NotDownloaded : ModelState()
    data class Downloading(val progress: Float) : ModelState()
    object Ready : ModelState()
    data class Error(val message: String) : ModelState()
}

@Singleton
class WhisperModelManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userPreferences: UserPreferencesDataStore,
    private val workManager: WorkManager
) {
    private val _modelState = MutableStateFlow<ModelState>(ModelState.NotDownloaded)
    val modelState: StateFlow<ModelState> = _modelState.asStateFlow()

    private val availableModels = listOf(
        ModelInfo(
            name = "whisper-tiny-en",
            language = "English",
            size = 75_000_000, // 75MB
            url = "https://huggingface.co/openai/whisper-tiny/resolve/main/model.safetensors"
        ),
        ModelInfo(
            name = "whisper-base-multilingual",
            language = "Multilingual",
            size = 150_000_000, // 150MB
            url = "https://huggingface.co/openai/whisper-base/resolve/main/model.safetensors"
        )
    )

    init {
        checkModelStatus()
    }

    private fun checkModelStatus() {
        val currentModel = getCurrentModel()
        if (isModelDownloaded(currentModel)) {
            _modelState.value = ModelState.Ready
        } else {
            _modelState.value = ModelState.NotDownloaded
        }
    }

    fun getAvailableModels(): List<ModelInfo> {
        return availableModels.map { model ->
            model.copy(isDownloaded = isModelDownloaded(model))
        }
    }

    fun getCurrentModel(): ModelInfo {
        val modelName = userPreferences.getSelectedModelName() ?: availableModels.first().name
        return availableModels.find { it.name == modelName } ?: availableModels.first()
    }

    suspend fun switchModel(modelInfo: ModelInfo) {
        if (!isModelDownloaded(modelInfo)) {
            downloadModel(modelInfo)
        }
        userPreferences.setSelectedModelName(modelInfo.name)
        checkModelStatus()
    }

    fun downloadModel(modelInfo: ModelInfo) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresStorageNotLow(true)
            .build()

        val inputData = workDataOf(
            "model_name" to modelInfo.name,
            "model_url" to modelInfo.url,
            "model_size" to modelInfo.size
        )

        val downloadRequest = OneTimeWorkRequestBuilder<ModelDownloadWorker>()
            .setConstraints(constraints)
            .setInputData(inputData)
            .build()

        workManager.enqueueUniqueWork(
            "model_download_${modelInfo.name}",
            ExistingWorkPolicy.KEEP,
            downloadRequest
        )

        // Observe work status
        workManager.getWorkInfoByIdLiveData(downloadRequest.id)
            .observeForever { workInfo ->
                when (workInfo.state) {
                    WorkInfo.State.RUNNING -> {
                        val progress = workInfo.progress.getFloat("progress", 0f)
                        _modelState.value = ModelState.Downloading(progress)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        _modelState.value = ModelState.Ready
                        checkModelStatus()
                    }
                    WorkInfo.State.FAILED -> {
                        val error = workInfo.outputData.getString("error") ?: "Download failed"
                        _modelState.value = ModelState.Error(error)
                    }
                    else -> {}
                }
            }
    }

    fun isModelDownloaded(modelInfo: ModelInfo): Boolean {
        val modelFile = getModelFile(modelInfo.name)
        return modelFile.exists() && modelFile.length() == modelInfo.size
    }

    fun getModelFile(modelName: String): File {
        return File(context.getExternalFilesDir("models"), "$modelName.tflite")
    }

    fun clearCache() {
        context.getExternalFilesDir("models")?.deleteRecursively()
        checkModelStatus()
    }

    companion object {
        private const val TAG = "WhisperModelManager"
    }
} 