package com.g.quash_sampler.ui.bugreport

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.g.quash_sampler.data.repository.BugRepository
import com.g.quash_sampler.data.repository.BugResult
import com.g.quash_sampler.domain.model.BugFormData
import com.g.quash_sampler.domain.model.BugFormValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class BugReportUiState(
    val formData: BugFormData = BugFormData(),
    val validation: BugFormValidation = BugFormValidation(isValid = false),
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

@HiltViewModel
class BugReportViewModel @Inject constructor(
    private val bugRepository: BugRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BugReportUiState())
    val uiState: StateFlow<BugReportUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        updateFormData { it.copy(title = title) }
        validateForm()
    }

    fun updateDescription(description: String) {
        updateFormData { it.copy(description = description) }
        validateForm()
    }

    fun updatePriority(priority: String) {
        updateFormData { it.copy(priority = priority) }
        validateForm()
    }

    fun updateCategory(category: String) {
        updateFormData { it.copy(category = category) }
        validateForm()
    }

    fun updateStepsToReproduce(steps: String) {
        updateFormData { it.copy(stepsToReproduce = steps) }
    }

    fun updateExpectedBehavior(behavior: String) {
        updateFormData { it.copy(expectedBehavior = behavior) }
    }

    fun updateActualBehavior(behavior: String) {
        updateFormData { it.copy(actualBehavior = behavior) }
    }

    fun updateEnvironment(environment: String) {
        updateFormData { it.copy(environment = environment) }
        validateForm()
    }

    fun updateReproducibility(reproducibility: String) {
        updateFormData { it.copy(reproducibility = reproducibility) }
    }

    fun updateSeverity(severity: String) {
        updateFormData { it.copy(severity = severity) }
    }

    fun addAttachment(file: File) {
        updateFormData {
            it.copy(attachments = it.attachments + file)
        }
    }

    fun removeAttachment(file: File) {
        updateFormData {
            it.copy(attachments = it.attachments - file)
        }
    }

    fun clearAttachments() {
        updateFormData {
            it.copy(attachments = emptyList())
        }
    }

    private fun updateFormData(update: (BugFormData) -> BugFormData) {
        _uiState.value = _uiState.value.copy(
            formData = update(_uiState.value.formData)
        )
    }

    private fun validateForm() {
        val formData = _uiState.value.formData
        val validation = BugFormValidation(
            isValid = formData.title.isNotBlank() &&
                     formData.description.isNotBlank() &&
                     formData.category.isNotBlank() &&
                     formData.priority.isNotBlank() &&
                     formData.environment.isNotBlank(),
            titleError = if (formData.title.isBlank()) "Title is required" else null,
            descriptionError = if (formData.description.isBlank()) "Description is required" else null,
            categoryError = if (formData.category.isBlank()) "Category is required" else null,
            priorityError = if (formData.priority.isBlank()) "Priority is required" else null,
            environmentError = if (formData.environment.isBlank()) "Environment is required" else null
        )

        _uiState.value = _uiState.value.copy(validation = validation)
    }

    fun submitBugReport(reporterId: String) {
        val currentState = _uiState.value

        if (!currentState.validation.isValid) {
            _uiState.value = currentState.copy(
                error = "Please fix all validation errors before submitting"
            )
            return
        }

        viewModelScope.launch {
            bugRepository.createBugReport(
                formData = currentState.formData,
                reporterId = reporterId
            ).collect { result ->
                when (result) {
                    is BugResult.Loading -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null
                        )
                    }
                    is BugResult.Success -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            isSubmitted = true,
                            successMessage = result.data.message,
                            error = null
                        )
                    }
                    is BugResult.Error -> {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun resetForm() {
        _uiState.value = BugReportUiState()
    }

    init {
        validateForm()
    }
}