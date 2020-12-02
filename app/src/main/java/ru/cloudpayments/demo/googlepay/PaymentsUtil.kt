/*
 * Copyright 2017 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.cloudpayments.demo.googlepay

import android.app.Activity
import com.google.android.gms.tasks.Task
import com.google.android.gms.wallet.*
import com.google.android.gms.wallet.Wallet.WalletOptions
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Contains helper static methods for dealing with the Payments API.
 *
 *
 * Many of the parameters used in the code are optional and are set here merely to call out their
 * existence. Please consult the documentation to learn more and feel free to remove ones not
 * relevant to your implementation.
 */
object PaymentsUtil {
	private val MICROS = BigDecimal(1000000.0)

	/**
	 * Creates an instance of [PaymentsClient] for use in an [Activity] using the
	 * environment and theme set in [ConstantsGPay].
	 *
	 * @param activity is the caller's activity.
	 */
	fun createPaymentsClient(activity: Activity?): PaymentsClient {
		val walletOptions = WalletOptions.Builder()
			.setEnvironment(ConstantsGPay.PAYMENTS_ENVIRONMENT)
			.build()
		return Wallet.getPaymentsClient(activity!!, walletOptions)
	}

	/**
	 * Builds [PaymentDataRequest] to be consumed by [PaymentsClient.loadPaymentData].
	 *
	 * @param transactionInfo contains the price for this transaction.
	 */
	fun createPaymentDataRequest(transactionInfo: TransactionInfo?): PaymentDataRequest {
		val paramsBuilder = PaymentMethodTokenizationParameters.newBuilder()
			.setPaymentMethodTokenizationType(
				WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_PAYMENT_GATEWAY
			)
			.addParameter("gateway", ConstantsGPay.GATEWAY_TOKENIZATION_NAME)
		for (param in ConstantsGPay.GATEWAY_TOKENIZATION_PARAMETERS) {
			paramsBuilder.addParameter(param!!.first!!, param.second!!)
		}
		return createPaymentDataRequest(transactionInfo, paramsBuilder.build())
	}

	private fun createPaymentDataRequest(
		transactionInfo: TransactionInfo?,
		params: PaymentMethodTokenizationParameters
	): PaymentDataRequest {
		return PaymentDataRequest.newBuilder()
			.setPhoneNumberRequired(false)
			.setEmailRequired(true)
			.setShippingAddressRequired(false)
			.setTransactionInfo(transactionInfo!!)
			.addAllowedPaymentMethods(ConstantsGPay.SUPPORTED_METHODS)
			.setCardRequirements(
				CardRequirements.newBuilder()
					.addAllowedCardNetworks(ConstantsGPay.SUPPORTED_NETWORKS)
					.setAllowPrepaidCards(true)
					.setBillingAddressRequired(true)
					.build()
			)
			.setPaymentMethodTokenizationParameters(params) // If the UI is not required, a returning user will not be asked to select
			// a card. Instead, the card they previously used will be returned
			// automatically (if still available).
			// Prior whitelisting is required to use this feature.
			.setUiRequired(true)
			.build()
	}

	/**
	 * Determines if the user is eligible to Pay with Google by calling
	 * [PaymentsClient.isReadyToPay]. The nature of this check depends on the methods set in
	 * [ConstantsGPay.SUPPORTED_METHODS].
	 *
	 *
	 * If [WalletConstants.PAYMENT_METHOD_CARD] is specified among supported methods, this
	 * function will return true even if the user has no cards stored. Please refer to the
	 * documentation for more information on how the check is performed.
	 *
	 * @param client used to send the request.
	 */
	fun isReadyToPay(client: PaymentsClient?): Task<Boolean?> {
		val request = IsReadyToPayRequest.newBuilder()
		for (allowedMethod in ConstantsGPay.SUPPORTED_METHODS) {
			request.addAllowedPaymentMethod(allowedMethod!!)
		}
		return client!!.isReadyToPay(request.build())
	}

	/**
	 * Builds [TransactionInfo] for use with [PaymentsUtil.createPaymentDataRequest].
	 *
	 *
	 * The price is not displayed to the user and must be in the following format: "12.34".
	 * [PaymentsUtil.microsToString] can be used to format the string.
	 *
	 * @param price total of the transaction.
	 */
	fun createTransaction(price: String?): TransactionInfo {
		return TransactionInfo.newBuilder()
			.setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
			.setTotalPrice(price!!)
			.setCurrencyCode(ConstantsGPay.CURRENCY_CODE)
			.build()
	}

	/**
	 * Converts micros to a string format accepted by [PaymentsUtil.createTransaction].
	 *
	 * @param micros value of the price.
	 */
	fun microsToString(micros: Long): String {
		return BigDecimal(micros).divide(MICROS).setScale(2, RoundingMode.HALF_EVEN).toString()
	}
}