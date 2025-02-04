package com.example.clicknote.domain.model

data class User(
    val id: String,
    val phoneNumber: String? = null,
    val email: String? = null,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false
) 