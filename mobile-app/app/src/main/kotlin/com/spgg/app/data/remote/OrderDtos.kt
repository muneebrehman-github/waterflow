package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class OrderItemRequestDto(val productId: Long, val quantity: Int)

@Serializable
data class PlaceOrderRequestDto(
    val addressId: Long,
    val items: List<OrderItemRequestDto>,
    val deliveryNotes: String?
)

@Serializable
data class OrderReasonRequestDto(val reason: String)

@Serializable
data class AssignDeliveryRequestDto(val deliveryBoyId: Long)

@Serializable
data class CompleteDeliveryRequestDto(val emptyBottlesCollected: Int?)

@Serializable
data class OrderItemResponseDto(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val depositAmount: Double,
    val lineTotal: Double
)

@Serializable
data class OrderResponseDto(
    val id: Long,
    val customerId: Long,
    val customerName: String?,
    val customerPhone: String?,
    val addressId: Long,
    val addressLabel: String?,
    val addressText: String?,
    val addressArea: String?,
    val deliveryBoyId: Long?,
    val status: String,
    val subtotal: Double,
    val deliveryCharge: Double,
    val discountAmount: Double,
    val orderAmount: Double,
    val totalDeposit: Double,
    val totalBottleQuantity: Int,
    val emptyBottlesCollected: Int?,
    val totalPayable: Double,
    val deliveryNotes: String?,
    val statusReason: String?,
    val items: List<OrderItemResponseDto>,
    val createdAt: String
)
