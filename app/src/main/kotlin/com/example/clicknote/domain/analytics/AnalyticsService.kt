package com.example.clicknote.domain.analytics

interface AnalyticsService {
    fun trackEvent(eventName: String, properties: Map<String, Any> = emptyMap())
    fun setUserProfile(userId: String, properties: Map<String, Any> = emptyMap())
    fun identify(userId: String)
    fun reset()
} 