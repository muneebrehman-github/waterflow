package com.spgg.app.domain

import kotlinx.serialization.Serializable

@Serializable
data class AuthSession(
    val accessToken: String,
    val refreshToken: String,
    val userId: Long,
    val fullName: String,
    val role: UserRole,
    val tenantId: Long?
)
