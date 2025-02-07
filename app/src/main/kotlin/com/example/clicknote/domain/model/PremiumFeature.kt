package com.example.clicknote.domain.model

enum class PremiumFeature(
    val id: String,
    val availableInFree: Boolean = false,
    val freeUsageLimit: Int = 0
) {
    TRANSCRIPTION("transcription", availableInFree = true, freeUsageLimit = 3),
    CLOUD_SYNC("cloud_sync"),
    OFFLINE_MODE("offline_mode"),
    SPEAKER_DETECTION("speaker_detection"),
    AI_SUMMARY("ai_summary"),
    CALL_RECORDING("call_recording"),
    AUDIO_ENHANCEMENT("audio_enhancement"),
    CUSTOM_FOLDERS("custom_folders", availableInFree = true),
    EXPORT("export", availableInFree = true),
    SEARCH("search", availableInFree = true);

    companion object {
        fun get(id: String): PremiumFeature? = values().find { it.id == id }
    }
} 