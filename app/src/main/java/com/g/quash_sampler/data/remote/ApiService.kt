package com.g.quash_sampler.data.remote

import com.g.quash_sampler.domain.model.*
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpRequest): Response<OtpResponse>

    @GET("api/status/{type}")
    suspend fun getStatus(@Path("type") type: String): Response<StatusResponse>

    @GET("api/metrics/daily")
    suspend fun getDailyMetrics(): Response<MetricsResponse>

    @GET("api/delayed")
    suspend fun getDelayed(@Query("ms") delayMs: Int? = null): Response<DelayResponse>

    @POST("api/orders")
    suspend fun createOrder(@Body request: OrderRequest): Response<OrderResponse>

    @GET("api/otp/latest")
    suspend fun getLatestOtp(): Response<ResponseBody>
}
