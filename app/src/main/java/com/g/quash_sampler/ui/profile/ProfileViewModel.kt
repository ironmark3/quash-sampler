package com.g.quash_sampler.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.g.quash_sampler.data.repository.ProfileRepository
import com.g.quash_sampler.data.repository.ProfileResult
import com.g.quash_sampler.domain.model.ProfileUpdateRequest
import com.g.quash_sampler.domain.model.User
import com.g.quash_sampler.domain.model.ProfileCompletionResponse
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val profileCompletion: ProfileCompletionResponse? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    fun loadProfile(userId: String) {
        viewModelScope.launch {
            profileRepository.getProfile(userId).collect { result ->
                when (result) {
                    is ProfileResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is ProfileResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            user = result.data,
                            isLoading = false,
                            error = null
                        )
                        // Also load profile completion
                        loadProfileCompletion(userId)
                    }
                    is ProfileResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun updateProfile(userId: String, profileData: ProfileUpdateRequest) {
        viewModelScope.launch {
            profileRepository.updateProfile(userId, profileData).collect { result ->
                when (result) {
                    is ProfileResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isUpdating = true,
                            error = null,
                            updateSuccess = false
                        )
                    }
                    is ProfileResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            user = result.data,
                            isUpdating = false,
                            error = null,
                            updateSuccess = true
                        )
                        // Reload profile completion after update
                        loadProfileCompletion(userId)
                    }
                    is ProfileResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isUpdating = false,
                            error = result.message,
                            updateSuccess = false
                        )
                    }
                }
            }
        }
    }

    private fun loadProfileCompletion(userId: String) {
        viewModelScope.launch {
            profileRepository.getProfileCompletion(userId).collect { result ->
                when (result) {
                    is ProfileResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            profileCompletion = result.data
                        )
                    }
                    is ProfileResult.Error -> {
                        // Don't show error for profile completion, it's secondary
                    }
                    is ProfileResult.Loading -> {
                        // Don't show loading for profile completion
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearUpdateSuccess() {
        _uiState.value = _uiState.value.copy(updateSuccess = false)
    }
}