package com.spgg.app.domain

data class LedgerEntry(
    val id: Long,
    val type: String,
    val amount: Double,
    val runningBalanceSnapshot: Double,
    val referenceType: String,
    val referenceId: Long,
    val description: String?,
    val createdAt: String
)

data class LedgerStatement(
    val balance: Double,
    val entries: List<LedgerEntry>
)
