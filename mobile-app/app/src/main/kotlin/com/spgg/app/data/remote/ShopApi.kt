package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface ShopApi {

    @POST("shops/register")
    suspend fun register(@Body request: ShopRegistrationRequestDto): ApiEnvelope<ShopResponseDto>
}
