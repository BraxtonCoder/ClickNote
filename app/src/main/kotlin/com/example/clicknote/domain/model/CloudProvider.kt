package com.example.clicknote.domain.model

enum class CloudProvider {
    LOCAL,
    FIREBASE,
    AWS,
    AZURE,
    GOOGLE;

    companion object {
        fun fromString(provider: String): CloudProvider {
            return try {
                valueOf(provider.uppercase())
            } catch (e: IllegalArgumentException) {
                LOCAL
            }
        }
    }
} 