package com.spgg.app.data.repository

import com.spgg.app.data.remote.AddressRequestDto
import com.spgg.app.data.remote.AddressResponseDto
import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.CustomerApi
import com.spgg.app.data.remote.CustomerProfileResponseDto
import com.spgg.app.data.remote.CustomerRegistrationRequestDto
import com.spgg.app.data.remote.CustomerSummaryResponseDto
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.ShopDirectoryResponseDto
import com.spgg.app.domain.ApiException
import com.spgg.app.domain.CustomerAddress
import com.spgg.app.domain.CustomerProfile
import com.spgg.app.domain.CustomerSummary
import com.spgg.app.domain.ShopDirectoryEntry
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class CustomerRepository @Inject constructor(
    private val api: CustomerApi,
    private val json: Json
) {

    suspend fun shopDirectory(): List<ShopDirectoryEntry> = runCatchingList { api.shopDirectory() }

    suspend fun register(
        shopId: Long,
        fullName: String,
        phone: String,
        email: String,
        password: String
    ): CustomerProfile = runCatchingProfile {
        api.register(CustomerRegistrationRequestDto(shopId, fullName, phone, email, password))
    }

    suspend fun getMyProfile(): CustomerProfile = runCatchingProfile { api.getMyProfile() }

    suspend fun listAddresses(): List<CustomerAddress> = runCatchingAddressList { api.listAddresses() }

    suspend fun createAddress(label: String, addressText: String, area: String?, isDefault: Boolean): CustomerAddress =
        runCatchingAddress { api.createAddress(AddressRequestDto(label, addressText, area, isDefault)) }

    suspend fun updateAddress(
        id: Long,
        label: String,
        addressText: String,
        area: String?,
        isDefault: Boolean
    ): CustomerAddress = runCatchingAddress { api.updateAddress(id, AddressRequestDto(label, addressText, area, isDefault)) }

    suspend fun setDefaultAddress(id: Long): CustomerAddress = runCatchingAddress { api.setDefaultAddress(id) }

    suspend fun deleteAddress(id: Long) {
        try {
            api.deleteAddress(id)
        } catch (e: HttpException) {
            throw ApiException(extractServerMessage(e))
        } catch (e: IOException) {
            throw ApiException("Unable to reach the server. Check your connection.")
        }
    }

    suspend fun listShopCustomers(): List<CustomerSummary> =
        wrap { api.listShopCustomers().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.map { it.toDomain() } }

    suspend fun getShopCustomer(id: Long): CustomerSummary =
        wrap { api.getShopCustomer(id).let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.toDomain() }

    private suspend fun runCatchingList(block: suspend () -> ApiEnvelope<List<ShopDirectoryResponseDto>>): List<ShopDirectoryEntry> =
        wrap { block().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.map { it.toDomain() } }

    private suspend fun runCatchingProfile(block: suspend () -> ApiEnvelope<CustomerProfileResponseDto>): CustomerProfile =
        wrap { block().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.toDomain() }

    private suspend fun runCatchingAddress(block: suspend () -> ApiEnvelope<AddressResponseDto>): CustomerAddress =
        wrap { block().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.toDomain() }

    private suspend fun runCatchingAddressList(block: suspend () -> ApiEnvelope<List<AddressResponseDto>>): List<CustomerAddress> =
        wrap { block().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.map { it.toDomain() } }

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

private fun ShopDirectoryResponseDto.toDomain() = ShopDirectoryEntry(id, name, description, address)

private fun CustomerProfileResponseDto.toDomain() = CustomerProfile(id, fullName, phone, email, status, shopId, shopName)

private fun AddressResponseDto.toDomain() = CustomerAddress(id, label, addressText, area, isDefault)

private fun CustomerSummaryResponseDto.toDomain() = CustomerSummary(id, fullName, phone, email, status)
