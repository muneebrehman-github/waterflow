package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class DayHoursDto(
    val day: String,
    val openTime: String?,
    val closeTime: String?,
    val closed: Boolean
)

@Serializable
data class DeliveryChargeTierDto(
    val minOrderValue: Double,
    val maxOrderValue: Double?,
    val charge: Double
)

@Serializable
data class ShopProfileResponseDto(
    val id: Long,
    val name: String,
    val description: String?,
    val ownerName: String,
    val email: String,
    val phone: String,
    val address: String?,
    val status: String,
    val businessHours: List<DayHoursDto>,
    val deliveryHours: List<DayHoursDto>,
    val deliveryRadiusKm: Double?,
    val minOrderValue: Double?,
    val deliveryChargeTiers: List<DeliveryChargeTierDto>
)

@Serializable
data class ShopProfileUpdateRequestDto(
    val name: String,
    val description: String?,
    val address: String?
)

@Serializable
data class HoursUpdateRequestDto(val hours: List<DayHoursDto>)

@Serializable
data class DeliverySettingsRequestDto(
    val deliveryRadiusKm: Double,
    val minOrderValue: Double,
    val deliveryChargeTiers: List<DeliveryChargeTierDto>
)
