package com.example.clicknote.util

import kotlinx.coroutines.delay
import kotlin.math.pow

sealed class NetworkResult<out T> {
    data class Success<T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String, val code: Int? = null) : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
}

suspend fun <T> withRetry(
    times: Int = 3,
    initialDelay: Long = 100,
    maxDelay: Long = 1000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var currentDelay = initialDelay
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Exception) {
            // Only retry on network-related exceptions
            if (!isNetworkException(e)) throw e
        }
        delay(currentDelay)
        currentDelay = (currentDelay * factor).toLong().coerceAtMost(maxDelay)
    }
    return block() // last attempt
}

private fun isNetworkException(throwable: Throwable): Boolean {
    return throwable is java.net.SocketTimeoutException ||
           throwable is java.net.UnknownHostException ||
           throwable is java.net.ConnectException ||
           throwable is java.io.IOException
}

suspend fun <T> safeApiCall(
    apiCall: suspend () -> T
): NetworkResult<T> = try {
    NetworkResult.Success(withRetry { apiCall() })
} catch (e: Exception) {
    NetworkResult.Error(
        message = e.message ?: "An unknown error occurred",
        code = when (e) {
            is retrofit2.HttpException -> e.code()
            else -> null
        }
    )
}

fun Throwable.toUserFriendlyMessage(): String = when (this) {
    is java.net.UnknownHostException -> "No internet connection"
    is java.net.SocketTimeoutException -> "Connection timed out"
    is java.net.ConnectException -> "Failed to connect to server"
    is retrofit2.HttpException -> when (code()) {
        401 -> "Authentication failed"
        403 -> "Access denied"
        404 -> "Resource not found"
        429 -> "Too many requests"
        500 -> "Server error"
        503 -> "Service unavailable"
        else -> "Network error (${code()})"
    }
    else -> message ?: "An unknown error occurred"
} 