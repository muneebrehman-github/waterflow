package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class ProductResponseDto(
    val id: Long,
    val name: String,
    val unitPrice: Double,
    val depositAmount: Double,
    val status: String,
    val displayOrder: Int
)

@Serializable
data class ProductCreateRequestDto(
    val name: String,
    val unitPrice: Double,
    val depositAmount: Double,
    val displayOrder: Int?
)

@Serializable
data class ProductUpdateRequestDto(
    val name: String,
    val depositAmount: Double,
    val displayOrder: Int?
)

@Serializable
data class ProductPriceUpdateRequestDto(val unitPrice: Double)

@Serializable
data class ProductPriceHistoryResponseDto(val price: Double, val changedAt: String)
