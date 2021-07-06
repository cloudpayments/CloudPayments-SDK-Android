package ru.cloudpayments.sdk.util

import com.google.android.gms.wallet.*
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import ru.cloudpayments.sdk.configuration.PaymentConfiguration
import ru.cloudpayments.sdk.ui.PaymentActivity

internal class GooglePayHandler {
	companion object {
		fun present(
			configuration: PaymentConfiguration,
			activity: PaymentActivity,
			requestCode: Int
		) {
			val paymentDataRequestJson =
				createPaymentDataRequest(
					configuration.paymentData.amount,
					configuration.paymentData.publicId,
					"CloudPayments"
				)
			val client = createPaymentsClient(activity)
			val request = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
			AutoResolveHelper.resolveTask(client.loadPaymentData(request), activity, requestCode)
		}

		/**
		 * Create a Google Pay API base request object with properties used in all requests.
		 *
		 * @return Google Pay API base request object.
		 * @throws JSONException
		 */
		private val baseRequest = JSONObject()
			.put("apiVersion", 2)
			.put("apiVersionMinor", 0)

		/**
		 * Card networks supported by your app and your gateway.
		 *
		 *
		 * @return Allowed card networks
		 * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
		 */
		private val allowedCardNetworks = JSONArray(
			listOf(
				"AMEX",
				"DISCOVER",
				"JCB",
				"MASTERCARD",
				"VISA"
			)
		)

		/**
		 * Card authentication methods supported by your app and your gateway.
		 *
		 *
		 * @return Allowed card authentication methods.
		 * @see [CardParameters](https://developers.google.com/pay/api/android/reference/object.CardParameters)
		 */
		private val allowedCardAuthMethods = JSONArray(
			listOf(
				"PAN_ONLY",
				"CRYPTOGRAM_3DS"
			)
		)

		/**
		 * Describe the expected returned payment data for the CARD payment method
		 *
		 * @return A CARD PaymentMethod describing accepted cards and optional fields.
		 * @throws JSONException
		 * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
		 */
		private fun cardPaymentMethod(publicMerchantId: String): JSONObject {
			val cardPaymentMethod = baseCardPaymentMethod()
				.put(
					"tokenizationSpecification",
					gatewayTokenizationSpecification(publicMerchantId)
				)

			return cardPaymentMethod
		}

		/**
		 * Gateway Integration: Identify your gateway and your app's gateway merchant identifier.
		 *
		 *
		 * The Google Pay API response will return an encrypted payment method capable of being charged
		 * by a supported gateway after payer authorization.
		 *
		 * @return Payment data tokenization for the CARD payment method.
		 * @throws JSONException
		 * @see [PaymentMethodTokenizationSpecification](https://developers.google.com/pay/api/android/reference/object.PaymentMethodTokenizationSpecification)
		 */
		private fun gatewayTokenizationSpecification(publicMerchantId: String): JSONObject {
			return JSONObject()
				.put("type", "PAYMENT_GATEWAY")
				.put(
					"parameters",
					JSONObject(
						mapOf("gateway" to "cloudpayments") + mapOf("gatewayMerchantId" to publicMerchantId)
					)
				)
		}

		/**
		 * Provide Google Pay API with a payment amount, currency, and amount status.
		 *
		 * @return information about the requested payment.
		 * @throws JSONException
		 * @see [TransactionInfo](https://developers.google.com/pay/api/android/reference/object.TransactionInfo)
		 */
		@Throws(JSONException::class)
		private fun getTransactionInfo(price: String): JSONObject {
			return JSONObject()
				.put("totalPrice", price)
				.put("totalPriceStatus", "FINAL")
				.put("currencyCode", "RUB")
		}

		private fun createPaymentDataRequest(
			formattedPrice: String,
			publicMerchantId: String,
			merchantName: String
		): JSONObject? {
			return try {
				baseRequest
					.put(
						"allowedPaymentMethods",
						JSONArray().put(cardPaymentMethod(publicMerchantId))
					)
					.put("transactionInfo", getTransactionInfo(formattedPrice))
					.put("merchantInfo", JSONObject().put("merchantName", merchantName))
			} catch (e: JSONException) {
				null
			}
		}

		private fun createPaymentsClient(activity: PaymentActivity): PaymentsClient {
			val walletOptions = Wallet.WalletOptions.Builder()
				.setEnvironment(GOOGLE_PAY_ENVIRONMENT)
				.build()
			return Wallet.getPaymentsClient(activity, walletOptions)
		}

		/**
		 * An object describing accepted forms of payment by your app, used to determine a viewer's
		 * readiness to pay.
		 *
		 * @return API version and payment methods supported by the app.
		 * @see [IsReadyToPayRequest](https://developers.google.com/pay/api/android/reference/object.IsReadyToPayRequest)
		 */
		fun isReadyToPayRequest(): JSONObject? {
			return try {
				baseRequest.put("allowedPaymentMethods", JSONArray().put(baseCardPaymentMethod()))
			} catch (e: JSONException) {
				null
			}
		}

		/**
		 * Describe your app's support for the CARD payment method.
		 *
		 *
		 * The provided properties are applicable to both an IsReadyToPayRequest and a
		 * PaymentDataRequest.
		 *
		 * @return A CARD PaymentMethod object describing accepted cards.
		 * @throws JSONException
		 * @see [PaymentMethod](https://developers.google.com/pay/api/android/reference/object.PaymentMethod)
		 */
		// Optionally, you can add billing address/phone number associated with a CARD payment method.
		private fun baseCardPaymentMethod(): JSONObject {
			val parameters = JSONObject()
				.put("allowedAuthMethods", allowedCardAuthMethods)
				.put("allowedCardNetworks", allowedCardNetworks)
			return JSONObject()
				.put("type", "CARD")
				.put("parameters", parameters)
		}

		suspend fun isReadyToMakeGooglePay(activity: PaymentActivity): Boolean {
			val jsonRequest = isReadyToPayRequest()?.toString() ?: return false
			val request = IsReadyToPayRequest.fromJson(jsonRequest)
			return createPaymentsClient(activity).isReadyToPay(request).await()
		}
	}
}

/**
 * PaymentData response object contains the payment information, as well as any additional
 * requested information, such as billing and shipping address.
 *
 * @param paymentData A response object returned by Google after a payer approves payment.
 * @see [Payment
 * Data](https://developers.google.com/pay/api/android/reference/object.PaymentData)
 */
fun PaymentData.handlePaymentSuccess(): String? {
	val paymentInformation = this.toJson() ?: return null
	return try {
		val paymentMethodData = JSONObject(paymentInformation).getJSONObject("paymentMethodData")
		val paymentToken = paymentMethodData
			.getJSONObject("tokenizationData")
			.getString("token")
		paymentToken
	} catch (e: JSONException) {
		null
	}
}