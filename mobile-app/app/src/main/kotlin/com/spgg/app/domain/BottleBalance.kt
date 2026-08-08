package com.spgg.app.domain

data class BottleBalance(
    val customerId: Long,
    val delivered: Int,
    val returned: Int,
    val lost: Int,
    val damaged: Int,
    val outstanding: Int
)

data class ShopBottleSummary(
    val totalDelivered: Int,
    val totalReturned: Int,
    val totalLost: Int,
    val totalDamaged: Int,
    val totalOutstanding: Int,
    val estimatedOutstandingValue: Double
)
