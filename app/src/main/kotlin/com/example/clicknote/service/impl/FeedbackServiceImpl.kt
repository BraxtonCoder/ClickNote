package com.example.clicknote.service.impl

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.example.clicknote.domain.service.FeedbackService
import com.example.clicknote.service.analytics.AnalyticsService
import com.example.clicknote.ui.components.FeedbackData
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FeedbackServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firestore: FirebaseFirestore,
    private val analyticsService: AnalyticsService
) : FeedbackService {

    private val _feedbackState = MutableStateFlow<FeedbackState>(FeedbackState.Idle)
    override val feedbackState: Flow<FeedbackState> = _feedbackState.asStateFlow()

    override suspend fun submitFeedback(feedback: FeedbackData): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            _feedbackState.value = FeedbackState.Submitting

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

            _feedbackState.value = FeedbackState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            _feedbackState.value = FeedbackState.Error(e.message ?: "Failed to submit feedback")
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

    sealed class FeedbackState {
        object Idle : FeedbackState()
        object Submitting : FeedbackState()
        object Success : FeedbackState()
        data class Error(val message: String) : FeedbackState()
    }
} 