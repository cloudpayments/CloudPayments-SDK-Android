package ru.cloudpayments.sdk.util

import com.google.android.gms.wallet.WalletConstants
import ru.cloudpayments.sdk.BuildConfig

val TAG = "Payment SDK"

val GOOGLE_PAY_ENVIRONMENT = if (BuildConfig.DEBUG)
	WalletConstants.ENVIRONMENT_TEST else
	WalletConstants.ENVIRONMENT_PRODUCTION

val GOOGLE_PAY_SUPPORTED_NETWORKS = arrayListOf(
	WalletConstants.CARD_NETWORK_VISA,
	WalletConstants.CARD_NETWORK_MASTERCARD,
	WalletConstants.CARD_NETWORK_AMEX,
	WalletConstants.CARD_NETWORK_DISCOVER,
	WalletConstants.CARD_NETWORK_JCB,
	WalletConstants.CARD_NETWORK_INTERAC)

val GOOGLE_PAY_SUPPORTED_METHODS = arrayListOf(
	WalletConstants.PAYMENT_METHOD_CARD,
	WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)