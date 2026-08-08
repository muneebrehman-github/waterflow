package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface StaffApi {

    @POST("shop/staff")
    suspend fun create(@Body request: StaffCreateRequestDto): ApiEnvelope<StaffSummaryResponseDto>

    @GET("shop/staff")
    suspend fun list(@Query("role") role: String? = null): ApiEnvelope<List<StaffSummaryResponseDto>>
}
