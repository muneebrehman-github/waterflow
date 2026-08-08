package com.spgg.app.data.repository

import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.StaffApi
import com.spgg.app.data.remote.StaffCreateRequestDto
import com.spgg.app.data.remote.StaffSummaryResponseDto
import com.spgg.app.domain.ApiException
import com.spgg.app.domain.StaffSummary
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class StaffRepository @Inject constructor(
    private val api: StaffApi,
    private val json: Json
) {

    suspend fun create(fullName: String, phone: String, email: String, password: String, role: String): StaffSummary =
        wrap { api.create(StaffCreateRequestDto(fullName, phone, email, password, role)).let {
            it.data ?: throw ApiException(it.message ?: "Something went wrong.")
        }.toDomain() }

    suspend fun list(role: String? = null): List<StaffSummary> =
        wrap { api.list(role).let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.map { it.toDomain() } }

    private suspend fun <T> wrap(block: suspend () -> T): T {
        try {
            return block()
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

private fun StaffSummaryResponseDto.toDomain() = StaffSummary(id, fullName, phone, email, role, status)
