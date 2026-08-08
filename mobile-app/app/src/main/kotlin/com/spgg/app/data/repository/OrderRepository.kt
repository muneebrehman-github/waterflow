package com.spgg.app.data.repository

import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.AssignDeliveryRequestDto
import com.spgg.app.data.remote.CompleteDeliveryRequestDto
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.OrderApi
import com.spgg.app.data.remote.OrderItemRequestDto
import com.spgg.app.data.remote.OrderItemResponseDto
import com.spgg.app.data.remote.OrderReasonRequestDto
import com.spgg.app.data.remote.OrderResponseDto
import com.spgg.app.data.remote.PlaceOrderRequestDto
import com.spgg.app.domain.ApiException
import com.spgg.app.domain.Order
import com.spgg.app.domain.OrderLineItem
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class OrderRepository @Inject constructor(
    private val api: OrderApi,
    private val json: Json
) {

    suspend fun placeOrder(addressId: Long, items: List<Pair<Long, Int>>, notes: String?): Order = wrapOrder {
        api.placeOrder(PlaceOrderRequestDto(addressId, items.map { OrderItemRequestDto(it.first, it.second) }, notes))
    }

    suspend fun listMyOrders(): List<Order> = wrapList { api.listMyOrders() }

    suspend fun getMyOrder(id: Long): Order = wrapOrder { api.getMyOrder(id) }

    suspend fun cancelMyOrder(id: Long): Order = wrapOrder { api.cancelMyOrder(id) }

    suspend fun listShopOrders(): List<Order> = wrapList { api.listShopOrders() }

    suspend fun getShopOrder(id: Long): Order = wrapOrder { api.getShopOrder(id) }

    suspend fun acceptOrder(id: Long): Order = wrapOrder { api.acceptOrder(id) }

    suspend fun rejectOrder(id: Long, reason: String): Order = wrapOrder { api.rejectOrder(id, OrderReasonRequestDto(reason)) }

    suspend fun prepareOrder(id: Long): Order = wrapOrder { api.prepareOrder(id) }

    suspend fun cancelShopOrder(id: Long, reason: String): Order =
        wrapOrder { api.cancelShopOrder(id, OrderReasonRequestDto(reason)) }

    suspend fun assignDelivery(id: Long, deliveryBoyId: Long): Order =
        wrapOrder { api.assignDelivery(id, AssignDeliveryRequestDto(deliveryBoyId)) }

    suspend fun listAssignedDeliveries(): List<Order> = wrapList { api.listAssignedDeliveries() }

    suspend fun getAssignedDelivery(id: Long): Order = wrapOrder { api.getAssignedDelivery(id) }

    suspend fun completeDelivery(id: Long, emptyBottlesCollected: Int?): Order =
        wrapOrder { api.completeDelivery(id, CompleteDeliveryRequestDto(emptyBottlesCollected)) }

    private suspend fun wrapOrder(block: suspend () -> ApiEnvelope<OrderResponseDto>): Order = wrap {
        block().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.toDomain()
    }

    private suspend fun wrapList(block: suspend () -> ApiEnvelope<List<OrderResponseDto>>): List<Order> = wrap {
        block().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.map { it.toDomain() }
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

private fun OrderItemResponseDto.toDomain() = OrderLineItem(productId, productName, quantity, unitPrice, depositAmount, lineTotal)

private fun OrderResponseDto.toDomain() = Order(
    id = id,
    customerId = customerId,
    customerName = customerName,
    customerPhone = customerPhone,
    addressId = addressId,
    addressLabel = addressLabel,
    addressText = addressText,
    addressArea = addressArea,
    deliveryBoyId = deliveryBoyId,
    status = status,
    subtotal = subtotal,
    deliveryCharge = deliveryCharge,
    discountAmount = discountAmount,
    orderAmount = orderAmount,
    totalDeposit = totalDeposit,
    totalBottleQuantity = totalBottleQuantity,
    emptyBottlesCollected = emptyBottlesCollected,
    totalPayable = totalPayable,
    deliveryNotes = deliveryNotes,
    statusReason = statusReason,
    items = items.map { it.toDomain() },
    createdAt = createdAt
)
