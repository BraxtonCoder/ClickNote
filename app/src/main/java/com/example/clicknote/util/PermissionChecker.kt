package com.example.clicknote.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionChecker @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun hasRecordAudioPermission(): Boolean =
        hasPermission(Manifest.permission.RECORD_AUDIO)

    fun hasNotificationPermission(): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            hasPermission(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            true
        }

    fun hasPhoneStatePermission(): Boolean =
        hasPermission(Manifest.permission.READ_PHONE_STATE)

    fun hasAllRequiredPermissions(): Boolean =
        hasRecordAudioPermission() &&
        hasPhoneStatePermission() &&
        hasNotificationPermission()

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED

    fun getMissingPermissions(): List<String> {
        val requiredPermissions = mutableListOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requiredPermissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        return requiredPermissions.filter { permission ->
            !hasPermission(permission)
        }
    }

    companion object {
        const val RECORD_AUDIO = Manifest.permission.RECORD_AUDIO
        const val POST_NOTIFICATIONS = Manifest.permission.POST_NOTIFICATIONS
        const val READ_PHONE_STATE = Manifest.permission.READ_PHONE_STATE
    }
} 