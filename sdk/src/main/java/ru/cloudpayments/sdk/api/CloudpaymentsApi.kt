package ru.cloudpayments.sdk.api

import android.net.Uri
import com.google.gson.Gson
import retrofit2.HttpException
import ru.cloudpayments.sdk.api.models.*
import java.net.URLDecoder
import javax.inject.Inject

class CloudpaymentsApi @Inject constructor(
	private val apiService: CloudpaymentsApiService,
	private val cardApiService: CloudpaymentsCardApiService
) {

	suspend fun charge(requestBody: PaymentRequestBody): CloudpaymentsTransactionResponse {
		return apiService.charge(requestBody)
	}

	suspend fun auth(requestBody: PaymentRequestBody): CloudpaymentsTransactionResponse {
		return apiService.auth(requestBody)
	}

	suspend fun postThreeDs(
		transactionId: String,
		threeDsCallbackId: String,
		paRes: String
	): CloudpaymentsThreeDsResponse {
		val md = ThreeDsMdData(
			transactionId = transactionId,
			threeDsCallbackId = threeDsCallbackId,
			successURL = THREE_DS_SUCCESS_URL,
			failURL = THREE_DS_FAIL_URL
		)
		val mdString = Gson().toJson(md)
		return try {
			val isSuccess = apiService.postThreeDs(ThreeDsRequestBody(md = mdString, paRes = paRes))
			CloudpaymentsThreeDsResponse(isSuccess, "")
		} catch (e: Exception) {
			if (e is HttpException && e.response()?.raw()?.isRedirect == true) {
				val url = e.response()?.raw()?.header("Location")
				when {
					url?.startsWith(THREE_DS_FAIL_URL) == true -> {
						if (url.contains(ErrorCodes.INSUFFICIENT_FUNDS.code.toString())) {
							return CloudpaymentsThreeDsResponse(false, ErrorCodes.INSUFFICIENT_FUNDS.message)
						}
						val uri = Uri.parse(url)
						val message =
							URLDecoder.decode(uri.getQueryParameter("CardHolderMessage"), "utf-8")
						CloudpaymentsThreeDsResponse(false, message)
					}
					url?.startsWith(THREE_DS_SUCCESS_URL) == true -> CloudpaymentsThreeDsResponse(
						true,
						null
					)
					else -> CloudpaymentsThreeDsResponse(false, null)
				}
			} else {
				CloudpaymentsThreeDsResponse(true, null)
			}
		}
	}

	suspend fun getBinInfo(firstSixDigits: String): CloudpaymentsBinInfo =
		if (firstSixDigits.length < 6) {
			throw CloudpaymentsTransactionError("You must specify the first 6 digits of the card number")
		} else {
			val firstSix = firstSixDigits.subSequence(0, 6).toString()
			val result = try {
				val response = cardApiService.getBinInfo(firstSix)
				response.binInfo ?: CloudpaymentsBinInfo("", "")
			} catch (e: Exception) {
				CloudpaymentsBinInfo("", "")
			}
			result
		}

	private companion object {
		private const val THREE_DS_SUCCESS_URL = "https://api.cloudpayments.ru/threeds/success"
		private const val THREE_DS_FAIL_URL = "https://api.cloudpayments.ru/threeds/fail"
	}
}