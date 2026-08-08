package com.spgg.app.data.repository

import com.spgg.app.data.remote.ApiEnvelope
import com.spgg.app.data.remote.ErrorEnvelope
import com.spgg.app.data.remote.PaymentApi
import com.spgg.app.data.remote.PaymentRequestDto
import com.spgg.app.data.remote.PaymentResponseDto
import com.spgg.app.data.remote.VoidPaymentRequestDto
import com.spgg.app.domain.ApiException
import com.spgg.app.domain.Payment
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import retrofit2.HttpException

@Singleton
class PaymentRepository @Inject constructor(
    private val api: PaymentApi,
    private val json: Json
) {

    suspend fun listForCustomer(customerId: Long): List<Payment> = wrapList { api.listForCustomer(customerId) }

    suspend fun record(customerId: Long, amount: Double, method: String, reference: String?): Payment =
        wrapPayment { api.record(customerId, PaymentRequestDto(amount, method, reference)) }

    suspend fun voidPayment(paymentId: Long, reason: String): Payment =
        wrapPayment { api.voidPayment(paymentId, VoidPaymentRequestDto(reason)) }

    private suspend fun wrapPayment(block: suspend () -> ApiEnvelope<PaymentResponseDto>): Payment = wrap {
        block().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.toDomain()
    }

    private suspend fun wrapList(block: suspend () -> ApiEnvelope<List<PaymentResponseDto>>): List<Payment> = wrap {
        block().let { it.data ?: throw ApiException(it.message ?: "Something went wrong.") }.map { it.toDomain() }
    }

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

private fun PaymentResponseDto.toDomain() = Payment(id, customerId, amount, method, reference, receiptNumber, status, voidReason, createdAt)
