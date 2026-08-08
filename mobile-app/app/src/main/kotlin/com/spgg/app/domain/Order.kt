package com.spgg.app.domain

data class OrderLineItem(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitPrice: Double,
    val depositAmount: Double,
    val lineTotal: Double
)

data class Order(
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
    val items: List<OrderLineItem>,
    val createdAt: String
)
