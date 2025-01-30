package com.example.clicknote.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SignUpUiState(
    val isLoading: Boolean = false,
    val isSignedIn: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class SignUpViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SignUpUiState())
    val uiState: StateFlow<SignUpUiState> = _uiState.asStateFlow()

    fun signUpWithEmail(name: String, email: String, password: String, confirmPassword: String) {
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Name is required")
            return
        }
        if (email.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Email is required")
            return
        }
        if (password.isBlank()) {
            _uiState.value = _uiState.value.copy(error = "Password is required")
            return
        }
        if (password != confirmPassword) {
            _uiState.value = _uiState.value.copy(error = "Passwords do not match")
            return
        }
        if (password.length < 6) {
            _uiState.value = _uiState.value.copy(error = "Password must be at least 6 characters")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                userRepository.signUpWithEmail(email, password)
                userRepository.updateProfile(name = name)
                _uiState.value = _uiState.value.copy(isSignedIn = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Sign up failed")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun signUpWithGoogle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                userRepository.signInWithGoogle()
                _uiState.value = _uiState.value.copy(isSignedIn = true)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message ?: "Google sign up failed")
            } finally {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    init {
        viewModelScope.launch {
            userRepository.getCurrentUser().collect { user ->
                _uiState.value = _uiState.value.copy(isSignedIn = user != null)
            }
        }
    }
} 