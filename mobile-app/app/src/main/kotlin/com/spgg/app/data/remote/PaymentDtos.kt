package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class PaymentRequestDto(val amount: Double, val method: String, val reference: String?)

@Serializable
data class VoidPaymentRequestDto(val reason: String)

@Serializable
data class PaymentResponseDto(
    val id: Long,
    val customerId: Long,
    val amount: Double,
    val method: String,
    val reference: String?,
    val receiptNumber: String,
    val status: String,
    val voidReason: String?,
    val createdAt: String
)
