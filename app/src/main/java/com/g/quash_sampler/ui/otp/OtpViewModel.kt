package com.g.quash_sampler.ui.otp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.g.quash_sampler.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OtpUiState(
    val otp: String = "",
    val sessionId: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val user: com.g.quash_sampler.domain.model.User? = null
)

@HiltViewModel
class OtpViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OtpUiState())
    val uiState: StateFlow<OtpUiState> = _uiState.asStateFlow()

    fun setSessionId(sessionId: String) {
        _uiState.update { current ->
            if (current.sessionId == sessionId) {
                current
            } else {
                current.copy(sessionId = sessionId, otp = "", error = null)
            }
        }
    }

    fun onOtpChanged(value: String) {
        _uiState.update { it.copy(otp = value, error = null) }
    }

    fun verifyOtp(onSuccess: (com.g.quash_sampler.domain.model.User?) -> Unit) {
        val currentState = _uiState.value
        if (currentState.sessionId.isBlank()) {
            _uiState.update { it.copy(error = "Session expired. Please login again.") }
            return
        }

        if (currentState.otp.length != 6) {
            _uiState.update { it.copy(error = "Enter the 6-digit code") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.verifyOtp(currentState.sessionId, currentState.otp)
                .onSuccess { response ->
                    if (response.success && response.token != null) {
                        // TODO: Save token to shared preferences or datastore
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = null,
                                otp = "",
                                user = response.user
                            )
                        }
                        onSuccess(response.user)
                    } else {
                        _uiState.update {
                            it.copy(
                                error = response.message.ifBlank { "OTP verification failed. Try again." },
                                isLoading = false
                            )
                        }
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message ?: "OTP verification failed. Try again.",
                            isLoading = false
                        )
                    }
                }
        }
    }
}
