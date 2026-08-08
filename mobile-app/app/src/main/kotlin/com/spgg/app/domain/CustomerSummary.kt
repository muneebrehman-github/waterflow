package com.spgg.app.domain

data class CustomerSummary(
    val id: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val status: String
)
