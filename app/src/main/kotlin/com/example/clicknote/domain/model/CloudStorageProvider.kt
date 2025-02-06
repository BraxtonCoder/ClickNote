package com.example.clicknote.domain.model

/**
 * Enum representing different cloud storage providers supported by the app
 */
enum class CloudStorageProvider {
    NONE,           // Local storage only
    GOOGLE_DRIVE,   // Google Drive storage
    DROPBOX,        // Dropbox storage
    ONE_DRIVE;      // OneDrive storage

    companion object {
        fun fromString(provider: String): CloudStorageProvider {
            return try {
                valueOf(provider.uppercase())
            } catch (e: IllegalArgumentException) {
                NONE
            }
        }

        /**
         * Returns true if the provider supports cloud sync
         */
        fun supportsCloudSync(provider: CloudStorageProvider): Boolean {
            return provider != NONE
        }
    }
} 