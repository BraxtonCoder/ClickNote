package com.example.clicknote.data

data class User(
    val id: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val isEmailVerified: Boolean = false,
    val isPremium: Boolean = false,
    val weeklyTranscriptionCount: Int = 0,
    val lastTranscriptionDate: Long = 0L
) 