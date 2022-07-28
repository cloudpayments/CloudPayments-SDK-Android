package ru.cloudpayments.sdk.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import ru.cloudpayments.sdk.api.models.CloudpaymentsBinInfoResponse
import ru.cloudpayments.sdk.api.models.PaymentRequestBody
import ru.cloudpayments.sdk.api.models.ThreeDsRequestBody
import ru.cloudpayments.sdk.api.models.CloudpaymentsTransactionResponse

interface CloudpaymentsApiService {
	@POST("/payments/cards/charge")
	suspend fun charge(@Body body: PaymentRequestBody): CloudpaymentsTransactionResponse

	@POST("/payments/cards/auth")
	suspend fun auth(@Body body: PaymentRequestBody): CloudpaymentsTransactionResponse

	@POST("/payments/ThreeDSCallback")
	suspend fun postThreeDs(@Body body: ThreeDsRequestBody): Boolean

	@GET("bins/info/{firstSixDigits}")
	suspend fun getBinInfo(@Path("firstSixDigits") firstSixDigits: String): CloudpaymentsBinInfoResponse
}