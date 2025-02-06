package com.example.clicknote.domain.model

data class User(
    val id: String,
    val email: String,
    val displayName: String? = null,
    val photoUrl: String? = null,
    val isEmailVerified: Boolean = false,
    val isAnonymous: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastSignInAt: Long = System.currentTimeMillis(),
    val metadata: Map<String, String> = emptyMap()
) {
    companion object {
        fun createAnonymous(): User = User(
            id = "anonymous_${System.currentTimeMillis()}",
            email = "",
            isAnonymous = true
        )
    }
} 