package ru.cloudpayments.demo.api

import ru.cloudpayments.demo.Constants
import ru.cloudpayments.sdk.api.models.*
import ru.cloudpayments.sdk.configuration.CloudpaymentsSDK

class PayApi {
	companion object {
		private val api = CloudpaymentsSDK.createApi(Constants.merchantPublicId)

		suspend fun charge(
			cardCryptogramPacket: String,
			cardHolderName: String?,
			amount: Int
		): CloudpaymentsTransaction? {
			// Параметры см. в PaymentRequestBody
			val body = PaymentRequestBody(
				amount = amount.toString(),
				currency = "RUB",
				ipAddress = "",
				name = cardHolderName.orEmpty(),
				cryptogram = cardCryptogramPacket
			)
			return api.charge(body).run {
				handleError().transaction
			}
		}

		suspend fun auth(
			cardCryptogramPacket: String,
			cardHolderName: String?,
			amount: Int
		): CloudpaymentsTransaction? {
			// Параметры см. в PaymentRequestBody
			val body = PaymentRequestBody(
				amount = amount.toString(),
				currency = "RUB",
				ipAddress = "",
				name = cardHolderName.orEmpty(),
				cryptogram = cardCryptogramPacket
			)
			return api.auth(body).run {
				handleError().transaction
			}
		}

		suspend fun postThreeDs(
			transactionId: String,
			threeDsCallbackId: String,
			paRes: String
		): CloudpaymentsThreeDsResponse {
			return api.postThreeDs(transactionId, threeDsCallbackId, paRes)
		}

		suspend fun getBinInfo(firstSixDigits: String): CloudpaymentsBinInfo {
			return api.getBinInfo(firstSixDigits)
		}
	}
}