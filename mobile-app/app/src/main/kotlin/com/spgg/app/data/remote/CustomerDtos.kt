package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class CustomerRegistrationRequestDto(
    val shopId: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val password: String
)

@Serializable
data class CustomerProfileResponseDto(
    val id: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val status: String,
    val shopId: Long,
    val shopName: String
)

@Serializable
data class AddressRequestDto(
    val label: String,
    val addressText: String,
    val area: String?,
    val isDefault: Boolean
)

@Serializable
data class AddressResponseDto(
    val id: Long,
    val label: String,
    val addressText: String,
    val area: String?,
    val isDefault: Boolean
)

@Serializable
data class ShopDirectoryResponseDto(
    val id: Long,
    val name: String,
    val description: String?,
    val address: String?
)

@Serializable
data class CustomerSummaryResponseDto(
    val id: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val status: String
)
