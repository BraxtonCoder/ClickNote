package com.example.clicknote.ui.viewmodel

import android.app.Activity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModel
import com.example.clicknote.util.PermissionHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class PermissionViewModel @Inject constructor(
    private val permissionHandler: PermissionHandler
) : ViewModel() {

    private val _missingPermissions = MutableStateFlow<List<String>>(emptyList())
    val missingPermissions: StateFlow<List<String>> = _missingPermissions.asStateFlow()

    private var permissionLauncher: ActivityResultLauncher<Array<String>>? = null

    fun registerPermissionLauncher(activity: Activity, launcher: ActivityResultLauncher<Array<String>>) {
        permissionLauncher = launcher
    }

    fun checkPermissions() {
        _missingPermissions.value = permissionHandler.getMissingPermissions()
    }

    fun requestPermissions() {
        val permissions = _missingPermissions.value
        if (permissions.isNotEmpty()) {
            permissionLauncher?.launch(permissions.toTypedArray())
        }
    }

    fun hasAllPermissions(): Boolean = permissionHandler.hasAllPermissions()

    fun hasAudioPermission(): Boolean = permissionHandler.hasAudioPermission()

    fun hasNotificationPermission(): Boolean = permissionHandler.hasNotificationPermission()

    fun hasPhoneStatePermission(): Boolean = permissionHandler.hasPhoneStatePermission()
} 