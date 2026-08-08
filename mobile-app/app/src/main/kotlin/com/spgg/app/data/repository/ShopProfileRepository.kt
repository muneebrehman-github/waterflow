package com.spgg.app.data.repository

import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.DayHoursDto
import com.spgg.app.data.remote.DeliveryChargeTierDto
import com.spgg.app.data.remote.DeliverySettingsRequestDto
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.HoursUpdateRequestDto
import com.spgg.app.data.remote.ShopProfileApi
import com.spgg.app.data.remote.ShopProfileResponseDto
import com.spgg.app.data.remote.ShopProfileUpdateRequestDto
import com.spgg.app.domain.ApiException
import com.spgg.app.domain.DayHours
import com.spgg.app.domain.DayOfWeek
import com.spgg.app.domain.DeliveryChargeTier
import com.spgg.app.domain.ShopProfile
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class ShopProfileRepository @Inject constructor(
    private val api: ShopProfileApi,
    private val json: Json
) {

    suspend fun getProfile(): ShopProfile = runCatchingApi { api.getProfile() }

    suspend fun updateProfile(name: String, description: String?, address: String?): ShopProfile =
        runCatchingApi { api.updateProfile(ShopProfileUpdateRequestDto(name, description, address)) }

    suspend fun updateBusinessHours(hours: List<DayHours>): ShopProfile =
        runCatchingApi { api.updateBusinessHours(HoursUpdateRequestDto(hours.map { it.toDto() })) }

    suspend fun updateDeliveryHours(hours: List<DayHours>): ShopProfile =
        runCatchingApi { api.updateDeliveryHours(HoursUpdateRequestDto(hours.map { it.toDto() })) }

    suspend fun updateDeliverySettings(
        deliveryRadiusKm: Double,
        minOrderValue: Double,
        tiers: List<DeliveryChargeTier>
    ): ShopProfile = runCatchingApi {
        api.updateDeliverySettings(
            DeliverySettingsRequestDto(deliveryRadiusKm, minOrderValue, tiers.map { it.toDto() })
        )
    }

    private suspend fun runCatchingApi(block: suspend () -> ApiEnvelope<ShopProfileResponseDto>): ShopProfile {
        try {
            val response = block()
            val data = response.data ?: throw ApiException(response.message ?: "Something went wrong.")
            return data.toDomain()
        } catch (e: HttpException) {
            throw ApiException(extractServerMessage(e))
        } catch (e: IOException) {
            throw ApiException("Unable to reach the server. Check your connection.")
        }
    }

    private fun extractServerMessage(e: HttpException): String {
        return runCatching {
            e.response()?.errorBody()?.string()?.let { body ->
                json.decodeFromString(ErrorEnvelope.serializer(), body).message
            }
        }.getOrNull() ?: "Something went wrong. Please try again."
    }
}

private fun DayHours.toDto() = DayHoursDto(day.name, openTime, closeTime, closed)

private fun DeliveryChargeTier.toDto() = DeliveryChargeTierDto(minOrderValue, maxOrderValue, charge)

private fun DayHoursDto.toDomain() = DayHours(DayOfWeek.valueOf(day), openTime, closeTime, closed)

private fun DeliveryChargeTierDto.toDomain() = DeliveryChargeTier(minOrderValue, maxOrderValue, charge)

private fun ShopProfileResponseDto.toDomain() = ShopProfile(
    id = id,
    name = name,
    description = description,
    ownerName = ownerName,
    email = email,
    phone = phone,
    address = address,
    status = status,
    businessHours = businessHours.map { it.toDomain() },
    deliveryHours = deliveryHours.map { it.toDomain() },
    deliveryRadiusKm = deliveryRadiusKm,
    minOrderValue = minOrderValue,
    deliveryChargeTiers = deliveryChargeTiers.map { it.toDomain() }
)
