package com.example.clicknote.util

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.result.ActivityResultLauncher
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.analytics.AnalyticsTracker
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PermissionManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val analyticsTracker: AnalyticsTracker
) : ViewModel() {

    private val _permissionState = MutableStateFlow<PermissionState>(PermissionState.Loading)
    val permissionState: StateFlow<PermissionState> = _permissionState.asStateFlow()

    private val requiredPermissions = mutableListOf(
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.READ_PHONE_STATE
    ).apply {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            add(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private var permissionRequestCount = mutableMapOf<String, Int>()
    private val maxRequestAttempts = 2

    init {
        checkPermissions()
    }

    fun checkPermissions() {
        viewModelScope.launch {
            val missingPermissions = getMissingPermissions()
            val permanentlyDeniedPermissions = getPermanentlyDeniedPermissions()
            
            _permissionState.value = when {
                missingPermissions.isEmpty() -> {
                    requiredPermissions.forEach { permission ->
                        analyticsTracker.trackPermissionGranted(permission)
                    }
                    PermissionState.Granted
                }
                permanentlyDeniedPermissions.isNotEmpty() -> {
                    permanentlyDeniedPermissions.forEach { permission ->
                        analyticsTracker.trackPermissionDenied(permission, isPermanent = true)
                    }
                    PermissionState.PermanentlyDenied(
                        deniedPermissions = permanentlyDeniedPermissions,
                        shouldShowSettings = true,
                        rationale = buildRationale(permanentlyDeniedPermissions)
                    )
                }
                else -> {
                    missingPermissions.forEach { permission ->
                        analyticsTracker.trackPermissionRequested(permission)
                    }
                    PermissionState.NeedsPermissions(
                        permissions = missingPermissions,
                        rationale = buildRationale(missingPermissions),
                        canRequestAgain = canRequestPermissions(missingPermissions)
                    )
                }
            }
        }
    }

    private fun getMissingPermissions(): List<String> {
        return requiredPermissions.filter { permission ->
            !hasPermission(permission)
        }
    }

    private fun getPermanentlyDeniedPermissions(): List<String> {
        return requiredPermissions.filter { permission ->
            !hasPermission(permission) && 
            permissionRequestCount[permission] ?: 0 >= maxRequestAttempts
        }
    }

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            permission
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun canRequestPermissions(permissions: List<String>): Boolean {
        return permissions.all { permission ->
            (permissionRequestCount[permission] ?: 0) < maxRequestAttempts
        }
    }

    private fun buildRationale(permissions: List<String>): String {
        return buildString {
            permissions.forEach { permission ->
                when (permission) {
                    Manifest.permission.RECORD_AUDIO -> 
                        append(context.getString(R.string.permission_microphone))
                    Manifest.permission.POST_NOTIFICATIONS -> 
                        append(context.getString(R.string.permission_notifications))
                    Manifest.permission.READ_PHONE_STATE -> 
                        append(context.getString(R.string.permission_phone))
                }
                append("\n")
            }
        }.trimEnd()
    }

    fun onPermissionsResult(
        permissions: Map<String, Boolean>
    ) {
        viewModelScope.launch {
            // Update request counts and track analytics
            permissions.forEach { (permission, isGranted) ->
                if (!isGranted) {
                    permissionRequestCount[permission] = 
                        (permissionRequestCount[permission] ?: 0) + 1
                    val isPermanent = permissionRequestCount[permission] ?: 0 >= maxRequestAttempts
                    analyticsTracker.trackPermissionDenied(permission, isPermanent)
                } else {
                    analyticsTracker.trackPermissionGranted(permission)
                }
            }

            val missingPermissions = permissions.filter { !it.value }.keys.toList()
            val permanentlyDeniedPermissions = getPermanentlyDeniedPermissions()

            _permissionState.value = when {
                missingPermissions.isEmpty() -> PermissionState.Granted
                permanentlyDeniedPermissions.isNotEmpty() -> PermissionState.PermanentlyDenied(
                    deniedPermissions = permanentlyDeniedPermissions,
                    shouldShowSettings = true,
                    rationale = buildRationale(permanentlyDeniedPermissions)
                )
                else -> PermissionState.NeedsPermissions(
                    permissions = missingPermissions,
                    rationale = buildRationale(missingPermissions),
                    canRequestAgain = canRequestPermissions(missingPermissions)
                )
            }
        }
    }

    fun resetPermissionRequestCount(permission: String) {
        analyticsTracker.trackPermissionReset(permission)
        permissionRequestCount.remove(permission)
        checkPermissions()
    }

    fun getSettingsIntent(): Intent {
        return Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
    }

    companion object {
        @Composable
        fun rememberPermissionState(): PermissionState {
            val viewModel = androidx.lifecycle.viewmodel.compose.viewModel<PermissionManager>()
            val state by viewModel.permissionState.collectAsState()

            LaunchedEffect(Unit) {
                viewModel.checkPermissions()
            }

            return state
        }
    }
}

sealed class PermissionState {
    object Loading : PermissionState()
    object Granted : PermissionState()
    data class NeedsPermissions(
        val permissions: List<String>,
        val rationale: String,
        val canRequestAgain: Boolean
    ) : PermissionState()
    data class PermanentlyDenied(
        val deniedPermissions: List<String>,
        val shouldShowSettings: Boolean,
        val rationale: String
    ) : PermissionState()
} 