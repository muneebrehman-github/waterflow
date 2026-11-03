package com.spgg.app.data.repository

import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.ShopApi
import com.spgg.app.data.remote.ShopRegistrationRequestDto
import com.spgg.app.data.remote.ShopResponseDto
import com.spgg.app.domain.ApiException
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class ShopRepository @Inject constructor(
    private val api: ShopApi,
    private val json: Json
) {

    suspend fun register(
        shopName: String,
        ownerName: String,
        email: String,
        phone: String,
        password: String,
        description: String?,
        address: String?
    ): ShopResponseDto = wrap {
        api.register(
            ShopRegistrationRequestDto(
                shopName = shopName,
                ownerName = ownerName,
                email = email,
                phone = phone,
                password = password,
                description = description?.ifBlank { null },
                address = address?.ifBlank { null }
            )
        )
    }

    private suspend fun wrap(block: suspend () -> ApiEnvelope<ShopResponseDto>): ShopResponseDto {
        try {
            val response = block()
            return response.data ?: throw ApiException(response.message ?: "Something went wrong.")
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
