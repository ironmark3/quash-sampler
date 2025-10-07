package com.g.quash_sampler.ui.login

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

data class LoginUiState(
    val identifier: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onIdentifierChanged(value: String) {
        _uiState.update { it.copy(identifier = value, error = null) }
    }

    fun login(onSuccess: (String) -> Unit) {
        val identifier = _uiState.value.identifier.trim()
        if (identifier.isBlank()) {
            _uiState.update { it.copy(error = "Please enter a valid phone or email") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            authRepository.login(identifier)
                .onSuccess { response ->
                    if (response.success && response.sessionId != null) {
                        _uiState.update { it.copy(isLoading = false, error = null) }
                        onSuccess(response.sessionId)
                    } else {
                        _uiState.update {
                            it.copy(
                                error = response.message.ifBlank { "Login failed. Please try again." },
                                isLoading = false
                            )
                        }
                    }
                }
                .onFailure { exception ->
                    _uiState.update {
                        it.copy(
                            error = exception.message ?: "Login failed. Please try again.",
                            isLoading = false
                        )
                    }
                }
        }
    }
}
