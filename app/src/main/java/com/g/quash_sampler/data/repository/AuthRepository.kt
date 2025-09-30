package com.g.quash_sampler.data.repository

import com.g.quash_sampler.data.remote.ApiService
import com.g.quash_sampler.domain.model.LoginRequest
import com.g.quash_sampler.domain.model.LoginResponse
import com.g.quash_sampler.domain.model.OtpRequest
import com.g.quash_sampler.domain.model.OtpResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun login(identifier: String): Result<LoginResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.login(LoginRequest(identifier))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("Login failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun verifyOtp(sessionId: String, otp: String): Result<OtpResponse> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.verifyOtp(OtpRequest(sessionId, otp))
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("OTP verification failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}