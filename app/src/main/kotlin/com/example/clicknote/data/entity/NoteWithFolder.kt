package com.example.clicknote.data.entity

import androidx.room.Embedded
import androidx.room.Relation

data class NoteWithFolder(
    @Embedded 
    val note: NoteEntity,
    
    @Relation(
        parentColumn = "folder_id",
        entityColumn = "id"
    )
    val folder: FolderEntity?
) 