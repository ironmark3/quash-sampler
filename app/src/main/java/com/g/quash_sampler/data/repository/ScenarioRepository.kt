package com.g.quash_sampler.data.repository

import com.g.quash_sampler.data.remote.ApiService
import com.g.quash_sampler.domain.model.DelayResponse
import com.g.quash_sampler.domain.model.MetricsResponse
import com.g.quash_sampler.domain.model.OrderRequest
import com.g.quash_sampler.domain.model.OrderResponse
import com.g.quash_sampler.domain.model.ScenarioCallResult
import com.g.quash_sampler.domain.model.StatusResponse
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScenarioRepository @Inject constructor(
    private val apiService: ApiService,
    moshi: Moshi
) {

    private val statusAdapter: JsonAdapter<StatusResponse> =
        moshi.adapter(StatusResponse::class.java).indent("  ")
    private val metricsAdapter: JsonAdapter<MetricsResponse> =
        moshi.adapter(MetricsResponse::class.java).indent("  ")
    private val delayAdapter: JsonAdapter<DelayResponse> =
        moshi.adapter(DelayResponse::class.java).indent("  ")
    private val orderAdapter: JsonAdapter<OrderResponse> =
        moshi.adapter(OrderResponse::class.java).indent("  ")

    suspend fun fetchStatus(type: String): Result<ScenarioCallResult> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getStatus(type)
            buildResult(response, statusAdapter)
        }
    }

    suspend fun fetchDailyMetrics(): Result<ScenarioCallResult> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getDailyMetrics()
            buildResult(response, metricsAdapter)
        }
    }

    suspend fun fetchDelayed(ms: Int?): Result<ScenarioCallResult> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getDelayed(ms)
            buildResult(response, delayAdapter)
        }
    }

    suspend fun createOrder(quantity: Int, channel: String = "android"): Result<ScenarioCallResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val response = apiService.createOrder(
                    OrderRequest(
                        productId = "demo_plan",
                        quantity = quantity,
                        channel = channel
                    )
                )
                buildResult(response, orderAdapter)
            }
        }

    suspend fun fetchLatestOtp(): Result<ScenarioCallResult> = withContext(Dispatchers.IO) {
        runCatching {
            val response = apiService.getLatestOtp()
            buildRawResult(response)
        }
    }

    private fun <T> buildResult(
        response: Response<T>,
        adapter: JsonAdapter<T>
    ): ScenarioCallResult {
        val statusCode = response.code()
        if (response.isSuccessful) {
            val body = response.body()
            val preview = body?.let { adapter.toJson(it) } ?: ""
            return ScenarioCallResult(statusCode, true, preview.ifBlank { "<empty body>" })
        }
        val errorPreview = response.errorBody()?.string().orEmpty()
        return ScenarioCallResult(
            statusCode = statusCode,
            isSuccessful = false,
            bodyPreview = errorPreview.ifBlank { response.message().ifBlank { "<no message>" } }
        )
    }

    private fun buildRawResult(response: Response<ResponseBody>): ScenarioCallResult {
        val statusCode = response.code()
        val text = if (response.isSuccessful) {
            response.body()?.string()
        } else {
            response.errorBody()?.string()
        }?.trim().orEmpty()

        return ScenarioCallResult(
            statusCode = statusCode,
            isSuccessful = response.isSuccessful,
            bodyPreview = if (text.isNotEmpty()) text else response.message().ifBlank { "<no message>" }
        )
    }
}
