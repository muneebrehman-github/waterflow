package com.spgg.app.domain

data class ShopProfile(
    val id: Long,
    val name: String,
    val description: String?,
    val ownerName: String,
    val email: String,
    val phone: String,
    val address: String?,
    val status: String,
    val businessHours: List<DayHours>,
    val deliveryHours: List<DayHours>,
    val deliveryRadiusKm: Double?,
    val minOrderValue: Double?,
    val deliveryChargeTiers: List<DeliveryChargeTier>
)
