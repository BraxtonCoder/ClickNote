package com.example.clicknote.domain.interfaces

interface FeedbackService {
    suspend fun submitFeedback(feedback: String, category: FeedbackCategory)
    suspend fun getFeedbackHistory(): List<FeedbackEntry>
    suspend fun ratePremiumFeature(featureId: String, rating: Int)
    
    enum class FeedbackCategory {
        BUG_REPORT,
        FEATURE_REQUEST,
        GENERAL_FEEDBACK,
        TRANSCRIPTION_QUALITY,
        APP_EXPERIENCE
    }
    
    data class FeedbackEntry(
        val id: String,
        val feedback: String,
        val category: FeedbackCategory,
        val timestamp: Long,
        val status: FeedbackStatus
    )
    
    enum class FeedbackStatus {
        SUBMITTED,
        UNDER_REVIEW,
        RESOLVED,
        IMPLEMENTED
    }
} 