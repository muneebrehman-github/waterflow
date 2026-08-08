package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class BottleBalanceResponseDto(
    val customerId: Long,
    val delivered: Int,
    val returned: Int,
    val lost: Int,
    val damaged: Int,
    val outstanding: Int
)

@Serializable
data class ShopBottleSummaryResponseDto(
    val totalDelivered: Int,
    val totalReturned: Int,
    val totalLost: Int,
    val totalDamaged: Int,
    val totalOutstanding: Int,
    val estimatedOutstandingValue: Double
)

@Serializable
data class BottleAdjustmentRequestDto(val quantity: Int, val type: String, val reason: String)
