package com.example.clicknote.domain.model

/**
 * Represents the source/origin of a note
 */
enum class NoteSource {
    MANUAL,         // User manually created note
    ACCESSIBILITY,  // Created via accessibility service
    CALL,          // Created from phone call
    IMPORT,        // Imported from external source
    SHARE,         // Shared from another app
    QUICK_TILE,    // Created from quick settings tile
    WIDGET;        // Created from widget

    companion object {
        fun fromString(source: String): NoteSource {
            return try {
                valueOf(source.uppercase())
            } catch (e: IllegalArgumentException) {
                MANUAL
            }
        }

        fun isExternalSource(source: NoteSource): Boolean {
            return when (source) {
                ACCESSIBILITY, CALL, IMPORT, SHARE, QUICK_TILE, WIDGET -> true
                else -> false
            }
        }

        fun requiresNotification(source: NoteSource): Boolean {
            return isExternalSource(source)
        }

        fun requiresPermission(source: NoteSource): Boolean {
            return when (source) {
                ACCESSIBILITY, CALL -> true
                else -> false
            }
        }
    }
} 