package com.example.clicknote.data.entity

import androidx.room.*
import com.example.clicknote.data.converter.RoomConverters
import com.example.clicknote.domain.model.Folder
import java.util.UUID

@Entity(
    tableName = "folders",
    indices = [
        Index("name", unique = true),
        Index("created_at")
    ]
)
@TypeConverters(RoomConverters::class)
data class FolderEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "color")
    val color: Int,
    
    @ColumnInfo(name = "note_count")
    val noteCount: Int = 0,
    
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_deleted")
    val isDeleted: Boolean = false,
    
    @ColumnInfo(name = "deleted_at")
    val deletedAt: Long? = null
) {
    fun toDomain() = Folder(
        id = id,
        name = name,
        color = color,
        noteCount = noteCount,
        createdAt = createdAt,
        modifiedAt = updatedAt,
        isDeleted = isDeleted,
        deletedAt = deletedAt
    )

    companion object {
        fun fromDomain(folder: Folder) = FolderEntity(
            id = folder.id,
            name = folder.name,
            color = folder.color,
            noteCount = folder.noteCount,
            createdAt = folder.createdAt,
            updatedAt = folder.modifiedAt,
            isDeleted = folder.isDeleted,
            deletedAt = folder.deletedAt
        )
    }
}

fun Folder.toFolderEntity(): FolderEntity = FolderEntity.fromDomain(this) 