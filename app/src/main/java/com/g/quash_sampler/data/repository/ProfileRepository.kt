package com.g.quash_sampler.data.repository

import com.g.quash_sampler.data.remote.ApiService
import com.g.quash_sampler.domain.model.ProfileUpdateRequest
import com.g.quash_sampler.domain.model.ProfileResponse
import com.g.quash_sampler.domain.model.ProfileCompletionResponse
import com.g.quash_sampler.domain.model.User
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import javax.inject.Inject
import javax.inject.Singleton

sealed class ProfileResult<out T> {
    data class Success<T>(val data: T) : ProfileResult<T>()
    data class Error(val message: String) : ProfileResult<Nothing>()
    data object Loading : ProfileResult<Nothing>()
}

@Singleton
class ProfileRepository @Inject constructor(
    private val apiService: ApiService
) {

    suspend fun getProfile(userId: String): Flow<ProfileResult<User>> = flow {
        emit(ProfileResult.Loading)
        try {
            val response = apiService.getProfile(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.user?.let { user ->
                    emit(ProfileResult.Success(user))
                } ?: emit(ProfileResult.Error("User data not found"))
            } else {
                val errorMessage = response.body()?.message ?: "Failed to fetch profile"
                emit(ProfileResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(ProfileResult.Error("Network error: ${e.message()}"))
        } catch (e: Exception) {
            emit(ProfileResult.Error("Unexpected error: ${e.message ?: "Unknown error"}"))
        }
    }

    suspend fun updateProfile(userId: String, profileData: ProfileUpdateRequest): Flow<ProfileResult<User>> = flow {
        emit(ProfileResult.Loading)
        try {
            val response = apiService.updateProfile(userId, profileData)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.user?.let { user ->
                    emit(ProfileResult.Success(user))
                } ?: emit(ProfileResult.Error("Updated user data not found"))
            } else {
                val errorMessage = response.body()?.message ?: "Failed to update profile"
                emit(ProfileResult.Error(errorMessage))
            }
        } catch (e: HttpException) {
            emit(ProfileResult.Error("Network error: ${e.message()}"))
        } catch (e: Exception) {
            emit(ProfileResult.Error("Unexpected error: ${e.message ?: "Unknown error"}"))
        }
    }

    suspend fun getProfileCompletion(userId: String): Flow<ProfileResult<ProfileCompletionResponse>> = flow {
        emit(ProfileResult.Loading)
        try {
            val response = apiService.getProfileCompletion(userId)
            if (response.isSuccessful && response.body()?.success == true) {
                response.body()?.let { completionData ->
                    emit(ProfileResult.Success(completionData))
                } ?: emit(ProfileResult.Error("Profile completion data not found"))
            } else {
                emit(ProfileResult.Error("Failed to fetch profile completion"))
            }
        } catch (e: HttpException) {
            emit(ProfileResult.Error("Network error: ${e.message()}"))
        } catch (e: Exception) {
            emit(ProfileResult.Error("Unexpected error: ${e.message ?: "Unknown error"}"))
        }
    }
}