package com.example.clicknote.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.firestore.DocumentSnapshot
import java.util.Date
import java.util.UUID
import com.example.clicknote.domain.model.SyncStatus

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val content: String,
    val transcription: String,
    val summary: String? = null,
    val audioPath: String? = null,
    val folderId: String? = null,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val modifiedAt: Long = System.currentTimeMillis(),
    val deletedAt: Long? = null,
    val syncStatus: SyncStatus = SyncStatus.IDLE
) {
    fun toFirestoreMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "content" to content,
        "transcription" to transcription,
        "summary" to summary,
        "audioPath" to audioPath,
        "folderId" to folderId,
        "isPinned" to isPinned,
        "isDeleted" to isDeleted,
        "createdAt" to createdAt,
        "modifiedAt" to modifiedAt,
        "deletedAt" to deletedAt
    )

    companion object {
        fun fromFirestore(doc: DocumentSnapshot): Note {
            return Note(
                id = doc.id,
                title = doc.getString("title") ?: "",
                content = doc.getString("content") ?: "",
                transcription = doc.getString("transcription") ?: "",
                summary = doc.getString("summary"),
                audioPath = doc.getString("audioPath"),
                folderId = doc.getString("folderId"),
                isPinned = doc.getBoolean("isPinned") ?: false,
                isDeleted = doc.getBoolean("isDeleted") ?: false,
                createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                modifiedAt = doc.getLong("modifiedAt") ?: System.currentTimeMillis(),
                deletedAt = doc.getLong("deletedAt"),
                syncStatus = SyncStatus.SYNCED
            )
        }
    }
}

enum class SyncStatus {
    PENDING,
    SYNCING,
    SYNCED,
    ERROR
} 