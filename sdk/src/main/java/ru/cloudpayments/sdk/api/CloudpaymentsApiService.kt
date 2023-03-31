package ru.cloudpayments.sdk.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.cloudpayments.sdk.api.models.CloudpaymentsBinInfoResponse
import ru.cloudpayments.sdk.api.models.PaymentRequestBody
import ru.cloudpayments.sdk.api.models.ThreeDsRequestBody
import ru.cloudpayments.sdk.api.models.CloudpaymentsTransactionResponse

interface CloudpaymentsApiService {
	@POST("payments/cards/charge")
	fun charge(@Body body: PaymentRequestBody): Single<CloudpaymentsTransactionResponse>

	@POST("payments/cards/auth")
	fun auth(@Body body: PaymentRequestBody): Single<CloudpaymentsTransactionResponse>

	@POST("payments/ThreeDSCallback")
	fun postThreeDs(@Body body: ThreeDsRequestBody): Single<Boolean>

	@GET("bins/info/{firstSixDigits}")
	fun getBinInfo(@Path("firstSixDigits") firstSixDigits: String): Single<CloudpaymentsBinInfoResponse>
}