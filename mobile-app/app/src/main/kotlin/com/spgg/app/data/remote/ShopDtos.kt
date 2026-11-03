package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ShopRegistrationRequestDto(
    val shopName: String,
    val ownerName: String,
    val email: String,
    val phone: String,
    val password: String,
    val description: String? = null,
    val address: String? = null
)

@Serializable
data class ShopResponseDto(
    val id: Long,
    val name: String,
    val description: String? = null,
    val ownerName: String,
    val email: String,
    val phone: String,
    val address: String? = null,
    val status: String,
    val rejectionReason: String? = null,
    val suspensionReason: String? = null,
    val createdAt: String? = null
)
