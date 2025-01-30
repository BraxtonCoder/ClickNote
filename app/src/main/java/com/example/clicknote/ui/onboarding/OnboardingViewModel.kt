package com.example.clicknote.ui.onboarding

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.Permission
import com.example.clicknote.domain.model.Permissions
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    application: Application,
    private val userPreferences: UserPreferencesDataStore
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        loadPermissions()
    }

    private fun loadPermissions() {
        val context = getApplication<Application>()
        val permissionStates = Permissions.list.map { permission ->
            val isGranted = ContextCompat.checkSelfPermission(
                context,
                permission.permission
            ) == PackageManager.PERMISSION_GRANTED
            permission to isGranted
        }.toMap()

        _uiState.update { state ->
            state.copy(
                permissions = permissionStates,
                currentPermissionIndex = 0,
                canProceed = !permissionStates.any { (permission, isGranted) ->
                    permission.isRequired && !isGranted
                }
            )
        }
    }

    fun onPermissionResult(permission: Permission, isGranted: Boolean) {
        _uiState.update { state ->
            val updatedPermissions = state.permissions.toMutableMap().apply {
                put(permission, isGranted)
            }
            state.copy(
                permissions = updatedPermissions,
                canProceed = !updatedPermissions.any { (permission, isGranted) ->
                    permission.isRequired && !isGranted
                }
            )
        }
        if (isGranted) {
            moveToNextPermission()
        }
    }

    fun moveToNextPermission() {
        _uiState.update { state ->
            state.copy(
                currentPermissionIndex = (state.currentPermissionIndex + 1)
                    .coerceAtMost(Permissions.list.size - 1)
            )
        }
    }

    fun moveToPreviousPermission() {
        _uiState.update { state ->
            state.copy(
                currentPermissionIndex = (state.currentPermissionIndex - 1)
                    .coerceAtLeast(0)
            )
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            userPreferences.setOnboardingCompleted(true)
        }
    }
}

data class OnboardingUiState(
    val permissions: Map<Permission, Boolean> = emptyMap(),
    val currentPermissionIndex: Int = 0,
    val canProceed: Boolean = false
) {
    val currentPermission: Permission?
        get() = Permissions.list.getOrNull(currentPermissionIndex)

    val isFirstPermission: Boolean
        get() = currentPermissionIndex == 0

    val isLastPermission: Boolean
        get() = currentPermissionIndex == Permissions.list.size - 1
} 