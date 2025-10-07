package com.g.quash_sampler.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.g.quash_sampler.data.repository.ScenarioRepository
import com.g.quash_sampler.domain.model.ScenarioCallResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

const val ACTION_STATUS_OK = "status_ok"
const val ACTION_STATUS_NOT_FOUND = "status_not_found"
const val ACTION_STATUS_SERVER_ERROR = "status_server_error"
const val ACTION_METRICS = "metrics_daily"
const val ACTION_DELAYED = "delayed"
const val ACTION_ORDER_OK = "order_ok"
const val ACTION_ORDER_INVALID = "order_invalid"
const val ACTION_LATEST_OTP = "latest_otp"

@HiltViewModel
class ScenarioViewModel @Inject constructor(
    private val scenarioRepository: ScenarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScenarioUiState())
    val uiState: StateFlow<ScenarioUiState> = _uiState.asStateFlow()

    fun triggerStatusOk() = executeAction(
        id = ACTION_STATUS_OK,
        label = "GET /api/status/ok"
    ) {
        scenarioRepository.fetchStatus("ok")
    }

    fun triggerStatusNotFound() = executeAction(
        id = ACTION_STATUS_NOT_FOUND,
        label = "GET /api/status/not-found"
    ) {
        scenarioRepository.fetchStatus("not-found")
    }

    fun triggerStatusServerError() = executeAction(
        id = ACTION_STATUS_SERVER_ERROR,
        label = "GET /api/status/server-error"
    ) {
        scenarioRepository.fetchStatus("server-error")
    }

    fun triggerDailyMetrics() = executeAction(
        id = ACTION_METRICS,
        label = "GET /api/metrics/daily"
    ) {
        scenarioRepository.fetchDailyMetrics()
    }

    fun triggerDelayed(ms: Int = 2000) = executeAction(
        id = ACTION_DELAYED,
        label = "GET /api/delayed?ms=$ms"
    ) {
        scenarioRepository.fetchDelayed(ms)
    }

    fun triggerCreateOrder(quantity: Int) = executeAction(
        id = if (quantity > 0) ACTION_ORDER_OK else ACTION_ORDER_INVALID,
        label = if (quantity > 0) "POST /api/orders (quantity=$quantity)" else "POST /api/orders (invalid quantity)"
    ) {
        scenarioRepository.createOrder(quantity)
    }

    fun triggerLatestOtp() = executeAction(
        id = ACTION_LATEST_OTP,
        label = "GET /api/otp/latest"
    ) {
        scenarioRepository.fetchLatestOtp()
    }

    fun clearError() {
        _uiState.update { state -> state.copy(error = null) }
    }

    private fun executeAction(
        id: String,
        label: String,
        request: suspend () -> Result<ScenarioCallResult>
    ) {
        viewModelScope.launch {
            _uiState.update { state ->
                state.copy(
                    inFlightActions = state.inFlightActions + id,
                    error = null
                )
            }

            val result = request()
            result
                .onSuccess { payload ->
                    val log = ScenarioLog(
                        label = label,
                        statusCode = payload.statusCode,
                        isSuccessful = payload.isSuccessful,
                        bodyPreview = payload.bodyPreview,
                        timestamp = System.currentTimeMillis()
                    )
                    _uiState.update { state ->
                        state.copy(
                            inFlightActions = state.inFlightActions - id,
                            logs = (listOf(log) + state.logs).take(20)
                        )
                    }
                }
                .onFailure { throwable ->
                    _uiState.update { state ->
                        state.copy(
                            inFlightActions = state.inFlightActions - id,
                            error = throwable.message ?: "Unexpected error"
                        )
                    }
                }
        }
    }
}

data class ScenarioUiState(
    val inFlightActions: Set<String> = emptySet(),
    val logs: List<ScenarioLog> = emptyList(),
    val error: String? = null
)

data class ScenarioLog(
    val label: String,
    val statusCode: Int,
    val isSuccessful: Boolean,
    val bodyPreview: String,
    val timestamp: Long
)
