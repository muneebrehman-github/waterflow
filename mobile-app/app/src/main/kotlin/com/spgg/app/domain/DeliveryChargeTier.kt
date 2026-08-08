package com.spgg.app.domain

/** maxOrderValue null means "and above" — the last/highest tier. */
data class DeliveryChargeTier(
    val minOrderValue: Double,
    val maxOrderValue: Double?,
    val charge: Double
)
