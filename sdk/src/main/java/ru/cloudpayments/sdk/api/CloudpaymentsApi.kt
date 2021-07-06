package ru.cloudpayments.sdk.api

import android.net.Uri
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
			CloudpaymentsThreeDsResponse(isSuccess, "", 0)
		} catch (e: Exception) {
			if (e is HttpException && e.response()?.raw()?.isRedirect == true) {
				val url = e.response()?.raw()?.header("Location")
				when {
					url?.startsWith(THREE_DS_FAIL_URL) == true -> {
						if (url.contains(ErrorCodes.INSUFFICIENT_FUNDS.code.toString())) {
							return CloudpaymentsThreeDsResponse(false, ErrorCodes.INSUFFICIENT_FUNDS.message, 0)
						}
						val uri = Uri.parse(url)
						val cardholderMessage = uri.getQueryParameter("CardHolderMessage")
						val reasonCode = uri.getQueryParameter("ReasonCode")?.toIntOrNull()
						val message = if (cardholderMessage != null) {
							withContext(Dispatchers.IO) {
								@Suppress("BlockingMethodInNonBlockingContext")
								URLDecoder.decode(cardholderMessage, "utf-8")
							}
						} else {
							""
						}
						CloudpaymentsThreeDsResponse(false, message, reasonCode)
					}
					url?.startsWith(THREE_DS_SUCCESS_URL) == true -> CloudpaymentsThreeDsResponse(true, null, 0)
					else -> CloudpaymentsThreeDsResponse(false, null, 0)
				}
			} else {
				CloudpaymentsThreeDsResponse(true, null, 0)
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