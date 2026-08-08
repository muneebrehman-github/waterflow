package com.spgg.app.domain

data class StaffSummary(
    val id: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val role: String,
    val status: String
)
