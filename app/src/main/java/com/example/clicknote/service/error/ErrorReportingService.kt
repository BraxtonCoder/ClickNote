package com.example.clicknote.service.error

import android.content.Context
import com.example.clicknote.service.analytics.AnalyticsService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.io.PrintWriter
import java.io.StringWriter

@Singleton
class ErrorReportingService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsService: AnalyticsService
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val _errors = MutableStateFlow<List<ErrorReport>>(emptyList())
    val errors: StateFlow<List<ErrorReport>> = _errors

    fun reportError(
        error: Throwable,
        screen: String,
        severity: ErrorSeverity = ErrorSeverity.ERROR,
        additionalInfo: Map<String, Any> = emptyMap()
    ) {
        val stackTrace = StringWriter().apply {
            error.printStackTrace(PrintWriter(this))
        }.toString()

        val report = ErrorReport(
            type = error::class.simpleName ?: "Unknown",
            message = error.message ?: "No message",
            stackTrace = stackTrace,
            timestamp = System.currentTimeMillis(),
            screen = screen,
            severity = severity,
            additionalInfo = additionalInfo
        )

        // Add to local list
        _errors.value = _errors.value + report

        // Track in analytics
        scope.launch {
            analyticsService.trackEvent(
                com.example.clicknote.service.analytics.AnalyticsEvent.ErrorOccurred(
                    type = report.type,
                    message = report.message,
                    screen = report.screen,
                    stackTrace = report.stackTrace
                )
            )
        }

        // Log to system
        android.util.Log.e(
            "ClickNote",
            "Error in $screen: ${error.message}",
            error
        )
    }

    fun clearErrors() {
        _errors.value = emptyList()
    }
}

data class ErrorReport(
    val type: String,
    val message: String,
    val stackTrace: String,
    val timestamp: Long,
    val screen: String,
    val severity: ErrorSeverity,
    val additionalInfo: Map<String, Any> = emptyMap()
)

enum class ErrorSeverity {
    DEBUG,
    INFO,
    WARNING,
    ERROR,
    CRITICAL
}

sealed class AppError(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause) {
    class NetworkError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class TranscriptionError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class StorageError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class AuthenticationError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class PermissionError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class AudioError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class DatabaseError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class CloudSyncError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class AIError(message: String, cause: Throwable? = null) : AppError(message, cause)
} 