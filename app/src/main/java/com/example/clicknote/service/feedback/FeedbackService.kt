package com.example.clicknote.service.feedback

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.clicknote.service.analytics.AnalyticsService
import com.example.clicknote.ui.components.FeedbackData
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers
import java.util.*
import kotlinx.coroutines.flow.Flow

@Singleton
class FeedbackService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val analyticsService: AnalyticsService
) : FeedbackService {
    suspend fun submitFeedback(feedback: FeedbackData) = withContext(Dispatchers.IO) {
        try {
            // Enrich feedback with device info
            val enrichedFeedback = feedback.copy(
                appVersion = getAppVersion(),
                deviceInfo = getDeviceInfo()
            )

            // Store in Firestore
            firestore.collection("feedback")
                .document(UUID.randomUUID().toString())
                .set(enrichedFeedback.toMap())
                .await()

            // Track in analytics
            analyticsService.trackEvent(
                com.example.clicknote.service.analytics.AnalyticsEvent.FeedbackSubmitted(
                    type = feedback.type.name,
                    rating = feedback.rating,
                    hasDescription = feedback.description != null,
                    hasContactEmail = feedback.contactEmail != null
                )
            )

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getAppVersion(): String {
        return try {
            val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            "${packageInfo.versionName} (${packageInfo.versionCode})"
        } catch (e: PackageManager.NameNotFoundException) {
            "Unknown"
        }
    }

    private fun getDeviceInfo(): Map<String, String> = buildMap {
        put("manufacturer", Build.MANUFACTURER)
        put("model", Build.MODEL)
        put("device", Build.DEVICE)
        put("os_version", Build.VERSION.RELEASE)
        put("sdk_version", Build.VERSION.SDK_INT.toString())
        put("language", Locale.getDefault().language)
    }

    private fun FeedbackData.toMap(): Map<String, Any?> = buildMap {
        put("type", type.name)
        put("rating", rating)
        put("description", description)
        put("contactEmail", contactEmail)
        put("timestamp", timestamp)
        put("appVersion", appVersion)
        put("deviceInfo", deviceInfo)
    }

    override suspend fun getFeedback(id: String): Feedback? {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }

    override fun getAllFeedback(): Flow<List<Feedback>> {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }

    override suspend fun updateFeedback(feedback: Feedback) {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }

    override suspend fun deleteFeedback(id: String) {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }

    override fun cleanup() {
        // Implementation needed
        throw UnsupportedOperationException("Method not implemented")
    }
}

// Add this to AnalyticsEvent.kt
data class FeedbackSubmitted(
    val type: String,
    val rating: Int,
    val hasDescription: Boolean,
    val hasContactEmail: Boolean
) : com.example.clicknote.service.analytics.AnalyticsEvent(
    name = "feedback_submitted",
    properties = mapOf(
        "type" to type,
        "rating" to rating,
        "has_description" to hasDescription,
        "has_contact_email" to hasContactEmail
    )
)

interface FeedbackService {
    suspend fun submitFeedback(feedback: FeedbackData)
    suspend fun getFeedback(id: String): Feedback?
    fun getAllFeedback(): Flow<List<Feedback>>
    suspend fun updateFeedback(feedback: Feedback)
    suspend fun deleteFeedback(id: String)
    fun cleanup()
}

data class Feedback(
    val id: String,
    val userId: String,
    val type: FeedbackType,
    val content: String,
    val timestamp: Long,
    val status: FeedbackStatus = FeedbackStatus.PENDING,
    val response: String? = null
)

enum class FeedbackType {
    BUG_REPORT,
    FEATURE_REQUEST,
    GENERAL_FEEDBACK,
    TRANSCRIPTION_ISSUE
}

enum class FeedbackStatus {
    PENDING,
    IN_PROGRESS,
    RESOLVED,
    CLOSED
} 