package com.spgg.app.data.repository

import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.ProductApi
import com.spgg.app.data.remote.ProductCreateRequestDto
import com.spgg.app.data.remote.ProductPriceHistoryResponseDto
import com.spgg.app.data.remote.ProductPriceUpdateRequestDto
import com.spgg.app.data.remote.ProductResponseDto
import com.spgg.app.data.remote.ProductUpdateRequestDto
import com.spgg.app.domain.ApiException
import com.spgg.app.domain.Product
import com.spgg.app.domain.ProductPriceHistoryEntry
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class ProductRepository @Inject constructor(
    private val api: ProductApi,
    private val json: Json
) {

    suspend fun create(name: String, unitPrice: Double, depositAmount: Double, displayOrder: Int?): Product =
        runCatchingApi { api.create(ProductCreateRequestDto(name, unitPrice, depositAmount, displayOrder)) }

    suspend fun listAll(): List<Product> =
        runCatchingListApi { api.listAll() }

    suspend fun listAvailable(): List<Product> =
        runCatchingListApi { api.listAvailable() }

    suspend fun update(id: Long, name: String, depositAmount: Double, displayOrder: Int?): Product =
        runCatchingApi { api.update(id, ProductUpdateRequestDto(name, depositAmount, displayOrder)) }

    suspend fun updatePrice(id: Long, unitPrice: Double): Product =
        runCatchingApi { api.updatePrice(id, ProductPriceUpdateRequestDto(unitPrice)) }

    suspend fun toggleAvailability(id: Long): Product =
        runCatchingApi { api.toggleAvailability(id) }

    suspend fun priceHistory(id: Long): List<ProductPriceHistoryEntry> = try {
        val response = api.priceHistory(id)
        response.data?.map { it.toDomain() } ?: throw ApiException(response.message ?: "Something went wrong.")
    } catch (e: HttpException) {
        throw ApiException(extractServerMessage(e))
    } catch (e: IOException) {
        throw ApiException("Unable to reach the server. Check your connection.")
    }

    suspend fun archive(id: Long) {
        try {
            api.archive(id)
        } catch (e: HttpException) {
            throw ApiException(extractServerMessage(e))
        } catch (e: IOException) {
            throw ApiException("Unable to reach the server. Check your connection.")
        }
    }

    private suspend fun runCatchingApi(block: suspend () -> ApiEnvelope<ProductResponseDto>): Product {
        try {
            val response = block()
            val data = response.data ?: throw ApiException(response.message ?: "Something went wrong.")
            return data.toDomain()
        } catch (e: HttpException) {
            throw ApiException(extractServerMessage(e))
        } catch (e: IOException) {
            throw ApiException("Unable to reach the server. Check your connection.")
        }
    }

    private suspend fun runCatchingListApi(block: suspend () -> ApiEnvelope<List<ProductResponseDto>>): List<Product> {
        try {
            val response = block()
            val data = response.data ?: throw ApiException(response.message ?: "Something went wrong.")
            return data.map { it.toDomain() }
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

private fun ProductResponseDto.toDomain() = Product(id, name, unitPrice, depositAmount, status, displayOrder)

private fun ProductPriceHistoryResponseDto.toDomain() = ProductPriceHistoryEntry(price, changedAt)
