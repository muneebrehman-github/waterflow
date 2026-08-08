package com.spgg.app.data.remote

import retrofit2.http.GET
import retrofit2.http.Path

interface LedgerApi {

    @GET("customers/me/ledger")
    suspend fun getMyLedger(): ApiEnvelope<LedgerStatementResponseDto>

    @GET("shop/customers/{id}/ledger")
    suspend fun getCustomerLedger(@Path("id") customerId: Long): ApiEnvelope<LedgerStatementResponseDto>
}
