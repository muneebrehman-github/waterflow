package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): ApiEnvelope<AuthResponseDto>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): ApiEnvelope<AccessTokenResponseDto>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequestDto): ApiEnvelope<Unit?>
}
