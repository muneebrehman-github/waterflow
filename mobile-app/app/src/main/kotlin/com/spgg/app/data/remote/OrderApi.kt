package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface OrderApi {

    // --- Customer-facing ---
    @POST("orders")
    suspend fun placeOrder(@Body request: PlaceOrderRequestDto): ApiEnvelope<OrderResponseDto>

    @GET("orders")
    suspend fun listMyOrders(): ApiEnvelope<List<OrderResponseDto>>

    @GET("orders/{id}")
    suspend fun getMyOrder(@Path("id") id: Long): ApiEnvelope<OrderResponseDto>

    @POST("orders/{id}/cancel")
    suspend fun cancelMyOrder(@Path("id") id: Long): ApiEnvelope<OrderResponseDto>

    // --- Shop Owner/Staff-facing ---
    @GET("shop/orders")
    suspend fun listShopOrders(): ApiEnvelope<List<OrderResponseDto>>

    @GET("shop/orders/{id}")
    suspend fun getShopOrder(@Path("id") id: Long): ApiEnvelope<OrderResponseDto>

    @POST("shop/orders/{id}/accept")
    suspend fun acceptOrder(@Path("id") id: Long): ApiEnvelope<OrderResponseDto>

    @POST("shop/orders/{id}/reject")
    suspend fun rejectOrder(@Path("id") id: Long, @Body request: OrderReasonRequestDto): ApiEnvelope<OrderResponseDto>

    @POST("shop/orders/{id}/prepare")
    suspend fun prepareOrder(@Path("id") id: Long): ApiEnvelope<OrderResponseDto>

    @POST("shop/orders/{id}/cancel")
    suspend fun cancelShopOrder(@Path("id") id: Long, @Body request: OrderReasonRequestDto): ApiEnvelope<OrderResponseDto>

    @POST("shop/orders/{id}/assign-delivery")
    suspend fun assignDelivery(@Path("id") id: Long, @Body request: AssignDeliveryRequestDto): ApiEnvelope<OrderResponseDto>

    // --- Delivery Boy-facing ---
    @GET("delivery/orders")
    suspend fun listAssignedDeliveries(): ApiEnvelope<List<OrderResponseDto>>

    @GET("delivery/orders/{id}")
    suspend fun getAssignedDelivery(@Path("id") id: Long): ApiEnvelope<OrderResponseDto>

    @POST("delivery/orders/{id}/complete")
    suspend fun completeDelivery(@Path("id") id: Long, @Body request: CompleteDeliveryRequestDto): ApiEnvelope<OrderResponseDto>
}
