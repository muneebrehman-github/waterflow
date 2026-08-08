package com.spgg.app.data.repository

import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.BottleAdjustmentRequestDto
import com.spgg.app.data.remote.BottleApi
import com.spgg.app.data.remote.BottleBalanceResponseDto
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.ShopBottleSummaryResponseDto
import com.spgg.app.domain.ApiException
import com.spgg.app.domain.BottleBalance
import com.spgg.app.domain.ShopBottleSummary
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class BottleRepository @Inject constructor(
    private val api: BottleApi,
    private val json: Json
) {

    suspend fun getMyBottleBalance(): BottleBalance = wrapBalance { api.getMyBottleBalance() }

    suspend fun getCustomerBottleBalance(customerId: Long): BottleBalance = wrapBalance { api.getCustomerBottleBalance(customerId) }

    suspend fun markAdjustment(customerId: Long, quantity: Int, type: String, reason: String): BottleBalance =
        wrapBalance { api.markAdjustment(customerId, BottleAdjustmentRequestDto(quantity, type, reason)) }

    suspend fun getShopSummary(): ShopBottleSummary = wrap {
        api.getShopSummary().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.toDomain()
    }

    private suspend fun wrapBalance(block: suspend () -> ApiEnvelope<BottleBalanceResponseDto>): BottleBalance = wrap {
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

private fun BottleBalanceResponseDto.toDomain() = BottleBalance(customerId, delivered, returned, lost, damaged, outstanding)

private fun ShopBottleSummaryResponseDto.toDomain() =
    ShopBottleSummary(totalDelivered, totalReturned, totalLost, totalDamaged, totalOutstanding, estimatedOutstandingValue)
