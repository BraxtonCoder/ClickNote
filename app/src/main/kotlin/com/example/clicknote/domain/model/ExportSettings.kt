package com.example.clicknote.domain.model

data class ExportSettings(
    val fontSize: FontSize = FontSize.MEDIUM,
    val includeTimestamps: Boolean = true,
    val includeSummary: Boolean = true,
    val includeDate: Boolean = true,
    val includeTitle: Boolean = true,
    val spacing: Spacing = Spacing.NORMAL
) {
    enum class FontSize(val titleSize: Float, val textSize: Float, val timestampSize: Float) {
        SMALL(16f, 12f, 8f),
        MEDIUM(18f, 14f, 10f),
        LARGE(20f, 16f, 12f)
    }
    
    enum class Spacing(val lineSpacing: Float, val paragraphSpacing: Float) {
        COMPACT(1.0f, 10f),
        NORMAL(1.2f, 15f),
        RELAXED(1.5f, 20f)
    }
} 