package com.example.clicknote.ui.auth

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.repository.UserRepository
import com.example.clicknote.service.impl.AuthServiceImpl.GoogleSignInIntentException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignInUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null,
    val signInIntent: Intent? = null
)

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignInUiState())
    val uiState: StateFlow<SignInUiState> = _uiState.asStateFlow()

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                userRepository.signInWithEmail(email, password)
                _uiState.value = _uiState.value.copy(isSignedIn = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Sign in failed")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun signInWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val result = userRepository.signInWithGoogle()
                result.fold(
                    onSuccess = {
                        _uiState.value = _uiState.value.copy(isSignedIn = true)
                    },
                    onFailure = { error ->
                        when (error) {
                            is GoogleSignInIntentException -> {
                                _uiState.value = _uiState.value.copy(signInIntent = error.intent)
                            }
                            else -> {
                                _uiState.value = _uiState.value.copy(error = error.message ?: "Google sign in failed")
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Google sign in failed")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSignInIntent() {
        _uiState.value = _uiState.value.copy(signInIntent = null)
    }

    init {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                _uiState.value = _uiState.value.copy(isSignedIn = user != null)
            }
        }
    }
} 