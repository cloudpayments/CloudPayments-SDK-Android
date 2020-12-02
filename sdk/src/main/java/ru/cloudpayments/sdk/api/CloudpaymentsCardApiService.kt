package ru.cloudpayments.sdk.api

import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.Query
import ru.cloudpayments.sdk.api.models.CloudpaymentsBinInfoResponse

interface CloudpaymentsCardApiService {
	@GET("BinInfo")
	fun getBinInfo(@Query("firstSixDigits") firstSixDigits: String): Single<CloudpaymentsBinInfoResponse>
}