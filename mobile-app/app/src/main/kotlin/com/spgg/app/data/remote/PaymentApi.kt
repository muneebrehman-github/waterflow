package com.spgg.app.data.remote

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface PaymentApi {

    @GET("shop/customers/{id}/payments")
    suspend fun listForCustomer(@Path("id") customerId: Long): ApiEnvelope<List<PaymentResponseDto>>

    @POST("shop/customers/{id}/payments")
    suspend fun record(@Path("id") customerId: Long, @Body request: PaymentRequestDto): ApiEnvelope<PaymentResponseDto>

    @POST("shop/payments/{id}/void")
    suspend fun voidPayment(@Path("id") paymentId: Long, @Body request: VoidPaymentRequestDto): ApiEnvelope<PaymentResponseDto>
}
