package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ApiEnvelope<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val timestamp: String? = null
)

@Serializable
data class LoginRequestDto(val email: String, val password: String)

@Serializable
data class RefreshRequestDto(val refreshToken: String)

@Serializable
data class AuthResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
    val fullName: String,
    val role: String,
    val tenantId: Long?
)

@Serializable
data class AccessTokenResponseDto(val accessToken: String)
