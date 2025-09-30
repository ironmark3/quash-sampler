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
    val phone: String? = null
)