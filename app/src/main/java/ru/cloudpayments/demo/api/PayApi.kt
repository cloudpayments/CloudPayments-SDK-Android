package ru.cloudpayments.demo.api

import io.reactivex.Observable
import ru.cloudpayments.sdk.api.models.*
import ru.cloudpayments.sdk.configuration.CloudpaymentsSDK
import ru.cloudpayments.demo.Constants

class PayApi {
	companion object {
		private val api = CloudpaymentsSDK.createApi(Constants.merchantPublicId)

		fun charge(cardCryptogramPacket: String, cardHolderName: String?, amount: Int): Observable<CloudpaymentsTransaction> {
			// Параметры см. в PaymentRequestBody
			val body = PaymentRequestBody(amount = amount.toString(),
										  currency = "RUB",
										  ipAddress = "",
										  name = cardHolderName.orEmpty(),
										  cryptogram = cardCryptogramPacket)
			return api.charge(body)
				.toObservable()
				.flatMap(CloudpaymentsTransactionResponse::handleError)
				.map { it.transaction }
		}

		fun auth(cardCryptogramPacket: String, cardHolderName: String?, amount: Int): Observable<CloudpaymentsTransaction> {
			// Параметры см. в PaymentRequestBody
			val body = PaymentRequestBody(amount = amount.toString(),
										  currency = "RUB",
										  ipAddress = "",
										  name = cardHolderName.orEmpty(),
										  cryptogram = cardCryptogramPacket)
			return api.auth(body)
				.toObservable()
				.flatMap(CloudpaymentsTransactionResponse::handleError)
				.map { it.transaction }
		}

		fun postThreeDs(transactionId: String, threeDsCallbackId: String, paRes: String): Observable<CloudpaymentsThreeDsResponse> {
			return api.postThreeDs(transactionId, threeDsCallbackId, paRes)
				.toObservable()
		}

		fun getBinInfo(firstSixDigits: String): Observable<CloudpaymentsBinInfo> {
			return api.getBinInfo(firstSixDigits)
					.toObservable()
		}
	}
}