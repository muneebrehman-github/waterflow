package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface BottleApi {

    @GET("customers/me/bottle-balance")
    suspend fun getMyBottleBalance(): ApiEnvelope<BottleBalanceResponseDto>

    @GET("shop/customers/{id}/bottle-balance")
    suspend fun getCustomerBottleBalance(@Path("id") customerId: Long): ApiEnvelope<BottleBalanceResponseDto>

    @POST("shop/customers/{id}/bottle-adjustments")
    suspend fun markAdjustment(@Path("id") customerId: Long, @Body request: BottleAdjustmentRequestDto): ApiEnvelope<BottleBalanceResponseDto>

    @GET("shop/bottles/summary")
    suspend fun getShopSummary(): ApiEnvelope<ShopBottleSummaryResponseDto>
}
