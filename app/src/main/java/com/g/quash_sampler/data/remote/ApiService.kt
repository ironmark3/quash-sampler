package com.g.quash_sampler.data.remote

import com.g.quash_sampler.domain.model.LoginRequest
import com.g.quash_sampler.domain.model.LoginResponse
import com.g.quash_sampler.domain.model.OtpRequest
import com.g.quash_sampler.domain.model.OtpResponse
import com.g.quash_sampler.domain.model.ProfileUpdateRequest
import com.g.quash_sampler.domain.model.ProfileResponse
import com.g.quash_sampler.domain.model.ProfileCompletionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpRequest): Response<OtpResponse>

    @GET("profile/{userId}")
    suspend fun getProfile(@Path("userId") userId: String): Response<ProfileResponse>

    @PUT("profile/{userId}")
    suspend fun updateProfile(
        @Path("userId") userId: String,
        @Body request: ProfileUpdateRequest
    ): Response<ProfileResponse>

    @GET("profile/{userId}/completion")
    suspend fun getProfileCompletion(@Path("userId") userId: String): Response<ProfileCompletionResponse>
}