package com.spgg.app.data.repository

import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.NotificationApi
import com.spgg.app.data.remote.RegisterDeviceTokenRequestDto
import com.spgg.app.domain.ApiException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class NotificationRepository @Inject constructor(
    private val api: NotificationApi,
    private val json: Json
) {

    suspend fun registerToken(token: String) {
        try {
            val response = api.registerDeviceToken(RegisterDeviceTokenRequestDto(token))
            if (!response.success) {
                throw ApiException(response.message ?: "Failed to register device token")
            }
        } catch (t: Throwable) {
            throw mapError(t)
        }
    }

    private fun mapError(t: Throwable): Throwable = when (t) {
        is HttpException -> {
            val message = runCatching {
                t.response()?.errorBody()?.string()?.let { body ->
                    json.decodeFromString(ErrorEnvelope.serializer(), body).message
                }
            }.getOrNull()
            ApiException(message ?: "Something went wrong. Please try again.")
        }
        is IOException -> ApiException("Unable to reach the server. Check your connection.")
        is ApiException -> t
        else -> ApiException(t.message ?: "Something went wrong. Please try again.")
    }
}
