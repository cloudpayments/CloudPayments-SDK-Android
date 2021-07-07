package ru.cloudpayments.sdk.api

import retrofit2.http.GET
import retrofit2.http.Query
import ru.cloudpayments.sdk.api.models.CloudpaymentsBinInfoResponse

interface CloudpaymentsCardApiService {
	@GET("BinInfo")
	suspend fun getBinInfo(@Query("firstSixDigits") firstSixDigits: String): CloudpaymentsBinInfoResponse
}