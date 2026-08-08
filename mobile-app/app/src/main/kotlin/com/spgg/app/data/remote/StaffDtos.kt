package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class StaffCreateRequestDto(
    val fullName: String,
    val phone: String,
    val email: String,
    val password: String,
    val role: String
)

@Serializable
data class StaffSummaryResponseDto(
    val id: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val role: String,
    val status: String
)
