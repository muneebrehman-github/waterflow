package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.POST
import retrofit2.http.Path

interface ProductApi {

    @POST("products")
    suspend fun create(@Body request: ProductCreateRequestDto): ApiEnvelope<ProductResponseDto>

    @GET("products")
    suspend fun listAll(): ApiEnvelope<List<ProductResponseDto>>

    @GET("products/catalog")
    suspend fun listAvailable(): ApiEnvelope<List<ProductResponseDto>>

    @PUT("products/{id}")
    suspend fun update(@Path("id") id: Long, @Body request: ProductUpdateRequestDto): ApiEnvelope<ProductResponseDto>

    @PUT("products/{id}/price")
    suspend fun updatePrice(
        @Path("id") id: Long,
        @Body request: ProductPriceUpdateRequestDto
    ): ApiEnvelope<ProductResponseDto>

    @PUT("products/{id}/availability")
    suspend fun toggleAvailability(@Path("id") id: Long): ApiEnvelope<ProductResponseDto>

    @GET("products/{id}/price-history")
    suspend fun priceHistory(@Path("id") id: Long): ApiEnvelope<List<ProductPriceHistoryResponseDto>>

    @DELETE("products/{id}")
    suspend fun archive(@Path("id") id: Long): ApiEnvelope<Unit?>
}
