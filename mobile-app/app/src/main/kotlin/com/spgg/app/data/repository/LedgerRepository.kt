package com.spgg.app.data.repository

import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.LedgerApi
import com.spgg.app.data.remote.LedgerEntryResponseDto
import com.spgg.app.data.remote.LedgerStatementResponseDto
import com.spgg.app.domain.ApiException
import com.spgg.app.domain.LedgerEntry
import com.spgg.app.domain.LedgerStatement
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class LedgerRepository @Inject constructor(
    private val api: LedgerApi,
    private val json: Json
) {

    suspend fun getMyLedger(): LedgerStatement = wrapStatement { api.getMyLedger() }

    suspend fun getCustomerLedger(customerId: Long): LedgerStatement = wrapStatement { api.getCustomerLedger(customerId) }

    private suspend fun wrapStatement(block: suspend () -> ApiEnvelope<LedgerStatementResponseDto>): LedgerStatement = wrap {
        block().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.toDomain()
    }

    private suspend fun <T> wrap(block: suspend () -> T): T {
        try {
            return block()
        } catch (e: HttpException) {
            throw ApiException(extractServerMessage(e))
        } catch (e: IOException) {
            throw ApiException("Unable to reach the server. Check your connection.")
        }
    }

    private fun extractServerMessage(e: HttpException): String {
        return runCatching {
            e.response()?.errorBody()?.string()?.let { body ->
                json.decodeFromString(ErrorEnvelope.serializer(), body).message
            }
        }.getOrNull() ?: "Something went wrong. Please try again."
    }
}

private fun LedgerEntryResponseDto.toDomain() = LedgerEntry(
    id = id,
    type = type,
    amount = amount,
    runningBalanceSnapshot = runningBalanceSnapshot,
    referenceType = referenceType,
    referenceId = referenceId,
    description = description,
    createdAt = createdAt
)

private fun LedgerStatementResponseDto.toDomain() = LedgerStatement(balance, entries.map { it.toDomain() })
