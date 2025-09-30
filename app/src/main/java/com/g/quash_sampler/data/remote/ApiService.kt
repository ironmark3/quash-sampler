package com.g.quash_sampler.data.remote

import com.g.quash_sampler.domain.model.LoginRequest
import com.g.quash_sampler.domain.model.LoginResponse
import com.g.quash_sampler.domain.model.OtpRequest
import com.g.quash_sampler.domain.model.OtpResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpRequest): Response<OtpResponse>
}