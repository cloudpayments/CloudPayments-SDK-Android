package ru.cloudpayments.sdk.api

import android.net.Uri
import com.google.gson.Gson
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import retrofit2.HttpException
import ru.cloudpayments.sdk.api.models.*
import java.net.URLDecoder
import javax.inject.Inject

class CloudpaymentsApi @Inject constructor(private val apiService: CloudpaymentsApiService) {
	companion object {
		private const val THREE_DS_SUCCESS_URL = "https://api.cloudpayments.ru/threeds/success"
		private const val THREE_DS_FAIL_URL = "https://api.cloudpayments.ru/threeds/fail"
	}
	fun charge(requestBody: PaymentRequestBody): Single<CloudpaymentsTransactionResponse> {
		return apiService.charge(requestBody)
			.subscribeOn(Schedulers.io())
	}

	fun auth(requestBody: PaymentRequestBody): Single<CloudpaymentsTransactionResponse> {
		return apiService.auth(requestBody)
			.subscribeOn(Schedulers.io())
	}

	fun postThreeDs(transactionId: String, threeDsCallbackId: String, paRes: String): Single<CloudpaymentsThreeDsResponse> {
		val md = ThreeDsMdData(transactionId = transactionId, threeDsCallbackId = threeDsCallbackId, successURL = THREE_DS_SUCCESS_URL, failURL = THREE_DS_FAIL_URL)
		val mdString = Gson().toJson(md)
		return apiService.postThreeDs(ThreeDsRequestBody(md = mdString, paRes = paRes))
			.subscribeOn(Schedulers.io())
			.map { CloudpaymentsThreeDsResponse(true, "", 0) }
			.onErrorReturn {
				val response: CloudpaymentsThreeDsResponse = if (it is HttpException && it.response()?.raw()!!.isRedirect) {
					val url = it.response()?.raw()?.header("Location")
					when {
						url?.startsWith(THREE_DS_FAIL_URL) == true -> {
							val uri = Uri.parse(url)
							val cardholderMessage = uri.getQueryParameter("CardHolderMessage")
							val reasonCode = uri.getQueryParameter("ReasonCode")?.toIntOrNull()
							val message = if (cardholderMessage != null) URLDecoder.decode(cardholderMessage, "utf-8") else ""
							CloudpaymentsThreeDsResponse(false, message, reasonCode)
						}
						url?.startsWith(THREE_DS_SUCCESS_URL) == true -> CloudpaymentsThreeDsResponse(true, null, 0)
						else -> CloudpaymentsThreeDsResponse(false, null, null)
					}
				} else {
					CloudpaymentsThreeDsResponse(true, null, 0)
				}

				response
			}
	}

	fun getBinInfo(firstSixDigits: String): Single<CloudpaymentsBinInfo> =
		if (firstSixDigits.length < 6) {
			Single.error(CloudpaymentsTransactionError("You must specify the first 6 digits of the card number"))
		} else {
			val firstSix = firstSixDigits.subSequence(0, 6).toString()
			apiService.getBinInfo(firstSix)
					.subscribeOn(Schedulers.io())
					.map { it.binInfo ?: CloudpaymentsBinInfo("", "") }
					.onErrorReturn { CloudpaymentsBinInfo("", "") }
		}
}