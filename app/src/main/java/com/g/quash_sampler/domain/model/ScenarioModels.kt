package com.g.quash_sampler.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class StatusResponse(
    val success: Boolean,
    val message: String,
    val timestamp: String? = null,
    val code: String? = null
)

@JsonClass(generateAdapter = true)
data class MetricsResponse(
    val success: Boolean,
    val generatedAt: String,
    val data: MetricsData
)

@JsonClass(generateAdapter = true)
data class MetricsData(
    val totals: MetricsTotals,
    val latestOtp: MetricsOtp,
    val recentSessions: List<MetricsSession>
)

@JsonClass(generateAdapter = true)
data class MetricsTotals(
    val signups: Int,
    val otpRequests: Int,
    val successfulSessions: Int
)

@JsonClass(generateAdapter = true)
data class MetricsOtp(
    val code: String,
    val channel: String,
    val sessionId: String,
    val issuedAt: String
)

@JsonClass(generateAdapter = true)
data class MetricsSession(
    val id: String,
    val status: String,
    val latencyMs: Int,
    val identifier: String
)

@JsonClass(generateAdapter = true)
data class DelayResponse(
    val success: Boolean,
    val delayMs: Int,
    val message: String
)

@JsonClass(generateAdapter = true)
data class OrderRequest(
    val productId: String,
    val quantity: Int,
    val channel: String = "app"
)

@JsonClass(generateAdapter = true)
data class OrderResponse(
    val success: Boolean,
    val order: OrderDetails? = null,
    val message: String? = null,
    val field: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderDetails(
    val id: String,
    val productId: String,
    val quantity: Int,
    val channel: String,
    val status: String
)

data class ScenarioCallResult(
    val statusCode: Int,
    val isSuccessful: Boolean,
    val bodyPreview: String
)
