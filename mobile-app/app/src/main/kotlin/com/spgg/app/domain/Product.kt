package com.spgg.app.domain

data class Product(
    val id: Long,
    val name: String,
    val unitPrice: Double,
    val depositAmount: Double,
    val status: String,
    val displayOrder: Int
) {
    val isAvailable: Boolean get() = status == "AVAILABLE"
}

data class ProductPriceHistoryEntry(val price: Double, val changedAt: String)
