package com.example.clicknote.domain.model

import com.google.firebase.firestore.PropertyName
import java.time.LocalDateTime

// These models will be shared across platforms through Firebase
data class FirebaseNote(
    @PropertyName("id") val id: String = "",
    @PropertyName("user_id") val userId: String = "",
    @PropertyName("title") val title: String = "",
    @PropertyName("content") val content: String = "",
    @PropertyName("created_at") val createdAt: Long = 0,
    @PropertyName("modified_at") val modifiedAt: Long = 0,
    @PropertyName("deleted_at") val deletedAt: Long? = null,
    @PropertyName("is_deleted") val isDeleted: Boolean = false,
    @PropertyName("is_pinned") val isPinned: Boolean = false,
    @PropertyName("is_long_form") val isLongForm: Boolean = false,
    @PropertyName("has_audio") val hasAudio: Boolean = false,
    @PropertyName("audio_url") val audioUrl: String? = null,
    @PropertyName("duration") val duration: Long = 0,
    @PropertyName("source") val source: String = "",
    @PropertyName("folder_id") val folderId: String? = null,
    @PropertyName("summary") val summary: String? = null,
    @PropertyName("key_points") val keyPoints: List<String> = emptyList(),
    @PropertyName("speakers") val speakers: List<String> = emptyList(),
    @PropertyName("platform") val platform: String = "android" // or "ios" or "web"
)

data class FirebaseFolder(
    @PropertyName("id") val id: String = "",
    @PropertyName("user_id") val userId: String = "",
    @PropertyName("name") val name: String = "",
    @PropertyName("color") val color: Int = 0,
    @PropertyName("created_at") val createdAt: Long = 0,
    @PropertyName("modified_at") val modifiedAt: Long = 0,
    @PropertyName("deleted_at") val deletedAt: Long? = null,
    @PropertyName("is_deleted") val isDeleted: Boolean = false
)

data class FirebaseUser(
    @PropertyName("id") val id: String = "",
    @PropertyName("email") val email: String = "",
    @PropertyName("displayName") val displayName: String? = null,
    @PropertyName("photoUrl") val photoUrl: String? = null,
    @PropertyName("subscriptionPlan") val subscriptionPlan: String = "FREE",
    @PropertyName("weeklyTranscriptionCount") val weeklyTranscriptionCount: Int = 0,
    @PropertyName("lastTranscriptionReset") val lastTranscriptionReset: Long = 0,
    @PropertyName("settings") val settings: UserSettings = UserSettings()
)

data class UserSettings(
    @PropertyName("theme") val theme: String = "system",
    @PropertyName("language") val language: String = "en",
    @PropertyName("saveAudio") val saveAudio: Boolean = true,
    @PropertyName("cloudSync") val cloudSync: Boolean = true,
    @PropertyName("cloudStorageType") val cloudStorageType: String = "FIREBASE",
    @PropertyName("notifications") val notifications: Boolean = true,
    @PropertyName("vibration") val vibration: Boolean = true
) 