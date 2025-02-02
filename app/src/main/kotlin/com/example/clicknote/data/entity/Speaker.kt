package com.example.clicknote.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "speakers")
data class Speaker(
    @PrimaryKey
    val id: String,
    val name: String,
    val voiceSignature: String?, // Voice fingerprint for speaker recognition
    val color: Int, // Color used to highlight this speaker's segments
    val isCustomName: Boolean = false, // Whether the name was customized by the user
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val updatedAt: LocalDateTime = LocalDateTime.now(),
    val lastUsed: LocalDateTime = LocalDateTime.now()
) 