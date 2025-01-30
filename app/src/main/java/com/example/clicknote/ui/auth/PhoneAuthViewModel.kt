package com.example.clicknote.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.clicknote.BuildConfig
import com.example.clicknote.domain.repository.UserRepository
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import dagger.hilt.android.lifecycle.HiltViewModel
import io.michaelrocks.libphonenumber.android.NumberParseException
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil
import io.michaelrocks.libphonenumber.android.Phonenumber
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class PhoneAuthViewModel @Inject constructor(
    application: Application,
    private val userRepository: UserRepository
) : AndroidViewModel(application) {

    private val phoneNumberUtil = PhoneNumberUtil.createInstance(application)
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<PhoneAuthUiState>(PhoneAuthUiState.Initial)
    val uiState: StateFlow<PhoneAuthUiState> = _uiState.asStateFlow()

    private val _countdownSeconds = MutableStateFlow<Int?>(null)
    val countdownSeconds: StateFlow<Int?> = _countdownSeconds.asStateFlow()

    private var countdownJob: Job? = null
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    private val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        override fun onVerificationCompleted(credential: PhoneAuthCredential) {
            viewModelScope.launch {
                signInWithPhoneAuthCredential(credential)
            }
        }

        override fun onVerificationFailed(e: FirebaseException) {
            val error = when (e) {
                is FirebaseAuthInvalidCredentialsException -> "Invalid phone number format"
                is FirebaseTooManyRequestsException -> "Too many requests. Please try again later"
                is FirebaseAuthMissingActivityForRecaptchaException -> "Unable to verify phone number"
                else -> "Verification failed: ${e.message}"
            }
            _uiState.value = PhoneAuthUiState.Error(error)
            stopCountdown()
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken
        ) {
            storedVerificationId = verificationId
            resendToken = token
            _uiState.value = PhoneAuthUiState.CodeSent
            startCountdown()
        }

        override fun onCodeAutoRetrievalTimeOut(verificationId: String) {
            storedVerificationId = verificationId
            _uiState.value = PhoneAuthUiState.CodeTimeout
            stopCountdown()
        }
    }

    fun startPhoneNumberVerification(phoneNumber: String, countryCode: String = "") {
        val formattedNumber = formatPhoneNumber(phoneNumber, countryCode)
        if (!isValidPhoneNumber(formattedNumber)) {
            _uiState.value = PhoneAuthUiState.Error("Invalid phone number format")
            return
        }

        _uiState.value = PhoneAuthUiState.Loading
        viewModelScope.launch {
            try {
                userRepository.signInWithPhoneNumber(formattedNumber, callbacks)
            } catch (e: Exception) {
                _uiState.value = PhoneAuthUiState.Error("Failed to start verification: ${e.message}")
                stopCountdown()
            }
        }
    }

    fun verifyPhoneNumberWithCode(code: String) {
        if (!isValidVerificationCode(code)) {
            _uiState.value = PhoneAuthUiState.Error("Invalid verification code")
            return
        }

        val verificationId = storedVerificationId
        if (verificationId == null) {
            _uiState.value = PhoneAuthUiState.Error("Verification ID not found")
            return
        }

        _uiState.value = PhoneAuthUiState.Loading
        viewModelScope.launch {
            try {
                val credential = PhoneAuthProvider.getCredential(verificationId, code)
                signInWithPhoneAuthCredential(credential)
            } catch (e: Exception) {
                _uiState.value = PhoneAuthUiState.Error("Failed to verify code: ${e.message}")
            }
        }
    }

    fun resendVerificationCode(phoneNumber: String, countryCode: String = "") {
        if (_countdownSeconds.value != null && _countdownSeconds.value!! > 0) {
            return
        }

        val formattedNumber = formatPhoneNumber(phoneNumber, countryCode)
        if (!isValidPhoneNumber(formattedNumber)) {
            _uiState.value = PhoneAuthUiState.Error("Invalid phone number format")
            return
        }

        val token = resendToken
        if (token == null) {
            _uiState.value = PhoneAuthUiState.Error("Unable to resend code")
            return
        }

        _uiState.value = PhoneAuthUiState.Loading
        viewModelScope.launch {
            try {
                userRepository.signInWithPhoneNumber(formattedNumber, callbacks)
            } catch (e: Exception) {
                _uiState.value = PhoneAuthUiState.Error("Failed to resend code: ${e.message}")
                stopCountdown()
            }
        }
    }

    private suspend fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        userRepository.signInWithPhoneCredential(credential)
            .onSuccess {
                _uiState.value = PhoneAuthUiState.Success(it)
                stopCountdown()
            }
            .onFailure { e ->
                val error = when (e) {
                    is FirebaseAuthInvalidCredentialsException -> "Invalid verification code"
                    else -> "Authentication failed: ${e.message}"
                }
                _uiState.value = PhoneAuthUiState.Error(error)
            }
    }

    private fun startCountdown(seconds: Int = 60) {
        stopCountdown()
        _countdownSeconds.value = seconds
        countdownJob = viewModelScope.launch {
            while (_countdownSeconds.value!! > 0) {
                delay(1000)
                _countdownSeconds.value = _countdownSeconds.value!! - 1
            }
        }
    }

    private fun stopCountdown() {
        countdownJob?.cancel()
        countdownJob = null
        _countdownSeconds.value = null
    }

    private fun formatPhoneNumber(phoneNumber: String, countryCode: String): String {
        return try {
            val number = phoneNumberUtil.parse(phoneNumber, countryCode)
            phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164)
        } catch (e: NumberParseException) {
            phoneNumber
        }
    }

    private fun isValidPhoneNumber(phoneNumber: String): Boolean {
        return try {
            val number = phoneNumberUtil.parse(phoneNumber, null)
            phoneNumberUtil.isValidNumber(number)
        } catch (e: NumberParseException) {
            false
        }
    }

    private fun isValidVerificationCode(code: String): Boolean {
        return code.matches(Regex("^\\d{6}$"))
    }

    override fun onCleared() {
        super.onCleared()
        stopCountdown()
    }

    fun getRegionDisplayName(countryCode: String): String {
        return try {
            val region = phoneNumberUtil.getRegionCodeForCountryCode(countryCode.removePrefix("+").toInt())
            java.util.Locale("", region).displayCountry
        } catch (e: Exception) {
            countryCode
        }
    }

    fun formatPhoneNumberForDisplay(phoneNumber: String, countryCode: String): String {
        return try {
            val number = phoneNumberUtil.parse(phoneNumber, countryCode)
            phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL)
        } catch (e: NumberParseException) {
            phoneNumber
        }
    }

    fun getExampleNumber(countryCode: String): String {
        return try {
            val region = phoneNumberUtil.getRegionCodeForCountryCode(countryCode.removePrefix("+").toInt())
            val example = phoneNumberUtil.getExampleNumber(region)
            phoneNumberUtil.format(example, PhoneNumberUtil.PhoneNumberFormat.NATIONAL)
        } catch (e: Exception) {
            ""
        }
    }
}

sealed class PhoneAuthUiState {
    object Initial : PhoneAuthUiState()
    object Loading : PhoneAuthUiState()
    object CodeSent : PhoneAuthUiState()
    object CodeTimeout : PhoneAuthUiState()
    data class Success(val user: com.example.clicknote.domain.model.User) : PhoneAuthUiState()
    data class Error(val message: String) : PhoneAuthUiState()
} 