package com.g.quash_sampler.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.g.quash_sampler.ui.theme.QuashSamplerTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(viewModel: ScenarioViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearError()
        }
    }

    val actions = remember(viewModel) {
        listOf(
            ScenarioActionUi(
                id = ACTION_STATUS_OK,
                title = "Status 200 (OK)",
                description = "Success payload with timestamp for JSONPath demos.",
                onClick = viewModel::triggerStatusOk
            ),
            ScenarioActionUi(
                id = ACTION_STATUS_NOT_FOUND,
                title = "Status 404 (Not Found)",
                description = "Failure payload to validate error handling.",
                onClick = viewModel::triggerStatusNotFound
            ),
            ScenarioActionUi(
                id = ACTION_STATUS_SERVER_ERROR,
                title = "Status 500 (Server Error)",
                description = "Intentional server error response for retry flows.",
                onClick = viewModel::triggerStatusServerError
            ),
            ScenarioActionUi(
                id = ACTION_METRICS,
                title = "Daily Metrics JSON",
                description = "Nested JSON with arrays for JSONPath extraction.",
                onClick = viewModel::triggerDailyMetrics
            ),
            ScenarioActionUi(
                id = ACTION_DELAYED,
                title = "Delayed Response",
                description = "Returns after 2s to test timeout handling.",
                onClick = { viewModel.triggerDelayed(2000) }
            ),
            ScenarioActionUi(
                id = ACTION_ORDER_OK,
                title = "Create Order (201)",
                description = "Valid order body to test POST success path.",
                onClick = { viewModel.triggerCreateOrder(quantity = 1) }
            ),
            ScenarioActionUi(
                id = ACTION_ORDER_INVALID,
                title = "Create Order (422)",
                description = "Invalid quantity to trigger validation error payload.",
                onClick = { viewModel.triggerCreateOrder(quantity = 0) }
            ),
            ScenarioActionUi(
                id = ACTION_LATEST_OTP,
                title = "Latest OTP (text/plain)",
                description = "Plain text response for regex extraction.",
                onClick = viewModel::triggerLatestOtp
            )
        )
    }

    HomeContent(
        uiState = uiState,
        actions = actions,
        snackbarHostState = snackbarHostState
    )
}

@Composable
private fun HomeContent(
    uiState: ScenarioUiState,
    actions: List<ScenarioActionUi>,
    snackbarHostState: SnackbarHostState
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("API Scenario Playground") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text(
                    text = "Trigger backend scenarios to validate status codes, JSONPath extraction, and regex prompts.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                )
            }

            items(actions) { action ->
                ScenarioActionCard(
                    action = action,
                    isLoading = uiState.inFlightActions.contains(action.id)
                )
            }

            if (uiState.logs.isNotEmpty()) {
                item {
                    Divider()
                    Text(
                        text = "Call History",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                items(uiState.logs) { log ->
                    ScenarioLogCard(log = log)
                }
            }
        }
    }
}

@Composable
private fun ScenarioActionCard(action: ScenarioActionUi, isLoading: Boolean) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = action.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = action.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            TextButton(
                onClick = action.onClick,
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Running...")
                } else {
                    Text("Execute")
                }
            }
        }
    }
}

@Composable
private fun ScenarioLogCard(log: ScenarioLog) {
    val formattedTime = remember(log.timestamp) {
        val formatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        formatter.format(Date(log.timestamp))
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = if (log.isSuccessful) Icons.Filled.CheckCircle else Icons.Filled.Error,
                    contentDescription = null,
                    tint = if (log.isSuccessful) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = log.label,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Status ${log.statusCode} • $formattedTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            SelectionContainer {
                Text(
                    text = log.bodyPreview,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

private data class ScenarioActionUi(
    val id: String,
    val title: String,
    val description: String,
    val onClick: () -> Unit
)

@Preview
@Composable
private fun HomeScreenPreview() {
    val sampleLogs = listOf(
        ScenarioLog(
            label = "GET /api/status/ok",
            statusCode = 200,
            isSuccessful = true,
            bodyPreview = "{\n  \"success\": true,\n  \"message\": \"Everything is operating normally\"\n}",
            timestamp = System.currentTimeMillis()
        ),
        ScenarioLog(
            label = "POST /api/orders (invalid quantity)",
            statusCode = 422,
            isSuccessful = false,
            bodyPreview = "{\"message\":\"quantity must be a positive number\"}",
            timestamp = System.currentTimeMillis()
        )
    )
    QuashSamplerTheme {
        HomeContent(
            uiState = ScenarioUiState(logs = sampleLogs),
            actions = emptyList(),
            snackbarHostState = SnackbarHostState()
        )
    }
}
