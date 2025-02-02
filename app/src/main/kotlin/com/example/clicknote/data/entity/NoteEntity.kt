package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.domain.model.Note
import java.util.UUID
import java.time.LocalDateTime

@Entity(
    tableName = "notes",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("folder_id"),
        Index("created_at"),
        Index("updated_at"),
        Index("sync_status")
    ]
)
@TypeConverters(RoomConverters::class)
data class NoteEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "content")
    val content: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "folder_id")
    val folderId: String? = null,

    @ColumnInfo(name = "is_pinned")
    val isPinned: Boolean = false,

    @ColumnInfo(name = "has_audio")
    val hasAudio: Boolean = false,

    @ColumnInfo(name = "audio_path")
    val audioPath: String? = null,

    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,

    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null,

    @ColumnInfo(name = "summary")
    val summary: String? = null,

    @ColumnInfo(name = "source")
    val source: String = "NOTE",

    @ColumnInfo(name = "sync_status")
    val syncStatus: Int = SYNC_STATUS_PENDING,

    @ColumnInfo(name = "audio_file_path")
    val audioFilePath: String? = null,

    @ColumnInfo(name = "speaker_profiles")
    val speakerProfiles: List<String> = emptyList()
) {
    companion object {
        const val SYNC_STATUS_PENDING = 0
        const val SYNC_STATUS_IN_PROGRESS = 1
        const val SYNC_STATUS_COMPLETED = 2
        const val SYNC_STATUS_FAILED = 3

        fun fromDomain(note: Note) = NoteEntity(
            id = note.id,
            title = note.title,
            content = note.content,
            createdAt = note.timestamp,
            folderId = note.folderId,
            hasAudio = note.hasAudio,
            audioPath = note.audioPath,
            isDeleted = note.isInTrash,
            isPinned = note.isPinned,
            deletedAt = note.deletedAt,
            summary = note.summary,
            source = note.source.toString(),
            audioFilePath = note.audioFilePath,
            speakerProfiles = note.speakerProfiles
        )
    }

    fun toDomain() = Note(
        id = id,
        title = title,
        content = content,
        timestamp = createdAt,
        folderId = folderId,
        hasAudio = hasAudio,
        audioPath = audioPath,
        isInTrash = isDeleted,
        isPinned = isPinned,
        deletedAt = deletedAt,
        summary = summary,
        source = source,
        audioFilePath = audioFilePath,
        speakerProfiles = speakerProfiles
    )
}

fun Note.toNoteEntity(): NoteEntity = NoteEntity.fromDomain(this) 