package com.g.quash_sampler.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.g.quash_sampler.data.repository.ProfileRepository
import com.g.quash_sampler.data.repository.ProfileResult
import com.g.quash_sampler.domain.model.ProfileUpdateRequest
import com.g.quash_sampler.domain.model.User
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingStep(
    val stepNumber: Int,
    val title: String,
    val description: String
)

data class OnboardingUiState(
    val currentStep: Int = 1,
    val totalSteps: Int = 4,
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isCompleting: Boolean = false,
    val isCompleted: Boolean = false,
    val showCompletionScreen: Boolean = false,

    // Form data
    val name: String = "",
    val address: String = "",
    val dateOfBirth: String = "",
    val selectedRole: String = "Reporter",

    // Validation
    val nameError: String? = null,
    val dateOfBirthError: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val steps = listOf(
        OnboardingStep(1, "Welcome!", "Let's get you started with your profile"),
        OnboardingStep(2, "Personal Info", "Tell us about yourself"),
        OnboardingStep(3, "Contact Details", "How can we reach you?"),
        OnboardingStep(4, "Your Role", "What best describes you?")
    )

    fun initializeOnboarding(user: User) {
        _uiState.value = _uiState.value.copy(
            user = user,
            name = user.name,
            address = user.address ?: "",
            dateOfBirth = user.dateOfBirth ?: "",
            selectedRole = user.role
        )
    }

    fun getCurrentStep(): OnboardingStep {
        return steps[_uiState.value.currentStep - 1]
    }

    fun nextStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep < _uiState.value.totalSteps) {
            _uiState.value = _uiState.value.copy(
                currentStep = currentStep + 1,
                error = null
            )
        }
    }

    fun previousStep() {
        val currentStep = _uiState.value.currentStep
        if (currentStep > 1) {
            _uiState.value = _uiState.value.copy(
                currentStep = currentStep - 1,
                error = null
            )
        }
    }

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = if (name.isBlank()) "Name is required" else null
        )
    }

    fun updateAddress(address: String) {
        _uiState.value = _uiState.value.copy(address = address)
    }

    fun updateDateOfBirth(dateOfBirth: String) {
        _uiState.value = _uiState.value.copy(
            dateOfBirth = dateOfBirth,
            dateOfBirthError = validateDateOfBirth(dateOfBirth)
        )
    }

    fun updateRole(role: String) {
        _uiState.value = _uiState.value.copy(selectedRole = role)
    }

    private fun validateDateOfBirth(date: String): String? {
        if (date.isBlank()) return null

        // Basic date format validation (YYYY-MM-DD)
        val dateRegex = Regex("^\\d{4}-\\d{2}-\\d{2}$")
        if (!dateRegex.matches(date)) {
            return "Please use format YYYY-MM-DD"
        }

        return null
    }

    fun canProceedToNextStep(): Boolean {
        return when (_uiState.value.currentStep) {
            1 -> true // Welcome step
            2 -> _uiState.value.name.isNotBlank() && _uiState.value.nameError == null
            3 -> _uiState.value.dateOfBirthError == null
            4 -> true // Role selection
            else -> false
        }
    }

    fun skipCurrentStep() {
        nextStep()
    }

    fun completeOnboarding(userId: String) {
        if (_uiState.value.isCompleting) return

        viewModelScope.launch {
            val request = ProfileUpdateRequest(
                name = _uiState.value.name,
                address = _uiState.value.address.takeIf { it.isNotBlank() },
                dateOfBirth = _uiState.value.dateOfBirth.takeIf { it.isNotBlank() },
                role = _uiState.value.selectedRole
            )

            profileRepository.updateProfile(userId, request).collect { result ->
                when (result) {
                    is ProfileResult.Loading -> {
                        _uiState.value = _uiState.value.copy(isCompleting = true)
                    }
                    is ProfileResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isCompleting = false,
                            showCompletionScreen = true,
                            user = result.data.copy(
                                name = _uiState.value.name, // Use the updated name
                                role = _uiState.value.selectedRole // Use the updated role
                            )
                        )
                    }
                    is ProfileResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isCompleting = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun finishOnboarding() {
        _uiState.value = _uiState.value.copy(isCompleted = true)
    }

    fun skipOnboarding() {
        _uiState.value = _uiState.value.copy(isCompleted = true)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun getProgressPercentage(): Float {
        return _uiState.value.currentStep.toFloat() / _uiState.value.totalSteps.toFloat()
    }
}