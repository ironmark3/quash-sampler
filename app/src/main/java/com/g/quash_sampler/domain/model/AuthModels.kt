package com.g.quash_sampler.domain.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class LoginRequest(
    val identifier: String // phone or email
)

@JsonClass(generateAdapter = true)
data class LoginResponse(
    val success: Boolean,
    val message: String,
    val sessionId: String? = null
)

@JsonClass(generateAdapter = true)
data class OtpRequest(
    val sessionId: String,
    val otp: String
)

@JsonClass(generateAdapter = true)
data class OtpResponse(
    val success: Boolean,
    val message: String,
    val token: String? = null,
    val user: User? = null
)

@JsonClass(generateAdapter = true)
data class User(
    val id: String,
    val name: String,
    val email: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val dateOfBirth: String? = null,
    val role: String = "Reporter",
    val isProfileComplete: Boolean = false,
    val profileCompletionPercentage: Int = 0
)

@JsonClass(generateAdapter = true)
data class ProfileUpdateRequest(
    val name: String,
    val address: String? = null,
    val dateOfBirth: String? = null,
    val role: String
)

@JsonClass(generateAdapter = true)
data class ProfileResponse(
    val success: Boolean,
    val message: String? = null,
    val user: User? = null
)

@JsonClass(generateAdapter = true)
data class ProfileCompletionResponse(
    val success: Boolean,
    val isComplete: Boolean,
    val completionPercentage: Int,
    val missingFields: List<String>
)