package com.spgg.app.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class LedgerEntryResponseDto(
    val id: Long,
    val type: String,
    val amount: Double,
    val runningBalanceSnapshot: Double,
    val referenceType: String,
    val referenceId: Long,
    val description: String?,
    val createdAt: String
)

@Serializable
data class LedgerStatementResponseDto(
    val balance: Double,
    val entries: List<LedgerEntryResponseDto>
)
