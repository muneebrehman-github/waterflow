package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT

interface ShopProfileApi {

    @GET("shop/profile")
    suspend fun getProfile(): ApiEnvelope<ShopProfileResponseDto>

    @PUT("shop/profile")
    suspend fun updateProfile(@Body request: ShopProfileUpdateRequestDto): ApiEnvelope<ShopProfileResponseDto>

    @PUT("shop/profile/business-hours")
    suspend fun updateBusinessHours(@Body request: HoursUpdateRequestDto): ApiEnvelope<ShopProfileResponseDto>

    @PUT("shop/profile/delivery-hours")
    suspend fun updateDeliveryHours(@Body request: HoursUpdateRequestDto): ApiEnvelope<ShopProfileResponseDto>

    @PUT("shop/profile/delivery-settings")
    suspend fun updateDeliverySettings(@Body request: DeliverySettingsRequestDto): ApiEnvelope<ShopProfileResponseDto>
}
