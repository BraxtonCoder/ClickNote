package com.example.clicknote.domain.service

import kotlinx.coroutines.flow.Flow

interface FeedbackService {
    suspend fun submitFeedback(feedback: Feedback)
    suspend fun getFeedbackHistory(): Flow<List<Feedback>>
    suspend fun getFeedbackStatus(feedbackId: String): FeedbackStatus
    
    data class Feedback(
        val id: String,
        val userId: String,
        val content: String,
        val type: FeedbackType,
        val timestamp: Long,
        val status: FeedbackStatus = FeedbackStatus.PENDING
    )
    
    enum class FeedbackType {
        BUG_REPORT,
        FEATURE_REQUEST,
        GENERAL_FEEDBACK,
        TRANSCRIPTION_ISSUE
    }
    
    enum class FeedbackStatus {
        PENDING,
        UNDER_REVIEW,
        RESOLVED,
        REJECTED
    }
} 