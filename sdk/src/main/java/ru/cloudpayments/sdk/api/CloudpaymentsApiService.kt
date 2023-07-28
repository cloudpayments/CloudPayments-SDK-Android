package ru.cloudpayments.sdk.api

import io.reactivex.Single
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.cloudpayments.sdk.api.models.CloudpaymentsBinInfoResponse
import ru.cloudpayments.sdk.api.models.CloudpaymentsGetTinkoffPayQrLinkResponse
import ru.cloudpayments.sdk.api.models.CloudpaymentsMerchantConfigurationResponse
import ru.cloudpayments.sdk.api.models.CloudpaymentsPublicKeyResponse
import ru.cloudpayments.sdk.api.models.PaymentRequestBody
import ru.cloudpayments.sdk.api.models.ThreeDsRequestBody
import ru.cloudpayments.sdk.api.models.CloudpaymentsTransactionResponse
import ru.cloudpayments.sdk.api.models.QrLinkStatusWaitBody
import ru.cloudpayments.sdk.api.models.QrLinkStatusWaitResponse
import ru.cloudpayments.sdk.api.models.TinkoffPayQrLinkBody

interface CloudpaymentsApiService {
	@POST("payments/cards/charge")
	fun charge(@Body body: PaymentRequestBody): Single<CloudpaymentsTransactionResponse>

	@POST("payments/cards/auth")
	fun auth(@Body body: PaymentRequestBody): Single<CloudpaymentsTransactionResponse>

	@POST("payments/ThreeDSCallback")
	fun postThreeDs(@Body body: ThreeDsRequestBody): Single<Boolean>

	@GET("bins/info/{firstSixDigits}")
	fun getBinInfo(@Path("firstSixDigits") firstSixDigits: String): Single<CloudpaymentsBinInfoResponse>

	@GET("payments/publickey")
	fun getPublicKey(): Single<CloudpaymentsPublicKeyResponse>

	@GET("merchant/configuration")
	fun getMerchantConfiguration(@Query("terminalPublicId") publicId: String): Single<CloudpaymentsMerchantConfigurationResponse>

	@POST("payments/qr/tinkoffpay/link")
	fun getTinkoffPayQrLink(@Body body: TinkoffPayQrLinkBody): Single<CloudpaymentsGetTinkoffPayQrLinkResponse>

	@POST("payments/qr/status/wait")
	fun qrLinkStatusWait(@Body body: QrLinkStatusWaitBody): Single<QrLinkStatusWaitResponse>
}