package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CustomerApi {

    @GET("shops/directory")
    suspend fun shopDirectory(): ApiEnvelope<List<ShopDirectoryResponseDto>>

    @POST("customers/register")
    suspend fun register(@Body request: CustomerRegistrationRequestDto): ApiEnvelope<CustomerProfileResponseDto>

    @GET("customers/me")
    suspend fun getMyProfile(): ApiEnvelope<CustomerProfileResponseDto>

    @GET("customers/me/addresses")
    suspend fun listAddresses(): ApiEnvelope<List<AddressResponseDto>>

    @POST("customers/me/addresses")
    suspend fun createAddress(@Body request: AddressRequestDto): ApiEnvelope<AddressResponseDto>

    @PUT("customers/me/addresses/{id}")
    suspend fun updateAddress(@Path("id") id: Long, @Body request: AddressRequestDto): ApiEnvelope<AddressResponseDto>

    @PUT("customers/me/addresses/{id}/default")
    suspend fun setDefaultAddress(@Path("id") id: Long): ApiEnvelope<AddressResponseDto>

    @DELETE("customers/me/addresses/{id}")
    suspend fun deleteAddress(@Path("id") id: Long): ApiEnvelope<Unit?>

    @GET("shop/customers")
    suspend fun listShopCustomers(): ApiEnvelope<List<CustomerSummaryResponseDto>>

    @GET("shop/customers/{id}")
    suspend fun getShopCustomer(@Path("id") id: Long): ApiEnvelope<CustomerSummaryResponseDto>
}
