package com.example.clicknote.domain.preferences

interface UserPreferences {
    fun isOnlineTranscriptionEnabled(): Boolean
    fun preferOfflineMode(): Boolean
    fun setOnlineTranscriptionEnabled(enabled: Boolean)
    fun setPreferOfflineMode(enabled: Boolean)
    fun getLastBackupTime(): Long
    fun setLastBackupTime(timestamp: Long)
    fun isAutoBackupEnabled(): Boolean
    fun setAutoBackupEnabled(enabled: Boolean)
    fun getAutoBackupInterval(): Int
    fun setAutoBackupInterval(hours: Int)
    fun isBackupOnWifiOnly(): Boolean
    fun setBackupOnWifiOnly(wifiOnly: Boolean)
} 