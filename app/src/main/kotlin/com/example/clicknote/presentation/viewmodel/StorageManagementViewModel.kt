package com.example.clicknote.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.model.CloudProvider
import com.example.clicknote.domain.preferences.UserPreferencesDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StorageManagementViewModel @Inject constructor(
    private val userPreferences: UserPreferencesDataStore
) : ViewModel() {

    val cloudProvider: StateFlow<CloudProvider> = userPreferences.cloudProvider
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CloudProvider.NONE
        )

    fun updateCloudProvider(provider: CloudProvider) {
        viewModelScope.launch {
            userPreferences.setCloudProvider(provider)
        }
    }
} 