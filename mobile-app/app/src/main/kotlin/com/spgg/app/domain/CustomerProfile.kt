package com.spgg.app.domain

data class CustomerProfile(
    val id: Long,
    val fullName: String,
    val phone: String,
    val email: String,
    val status: String,
    val shopId: Long,
    val shopName: String
)

data class CustomerAddress(
    val id: Long,
    val label: String,
    val addressText: String,
    val area: String?,
    val isDefault: Boolean
)

data class ShopDirectoryEntry(
    val id: Long,
    val name: String,
    val description: String?,
    val address: String?
)
