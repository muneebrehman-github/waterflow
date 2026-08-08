package com.spgg.app.domain

data class Payment(
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
