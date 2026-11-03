package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.POST

interface NotificationApi {

    @POST("notifications/device-token")
    suspend fun registerDeviceToken(@Body request: RegisterDeviceTokenRequestDto): ApiEnvelope<Unit?>
}
