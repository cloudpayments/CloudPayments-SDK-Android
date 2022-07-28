package ru.cloudpayments.sdk.models

import ru.cloudpayments.sdk.configuration.CloudpaymentsSDK

data class Transaction (
	val transactionId: Int?,
	val status: CloudpaymentsSDK.TransactionStatus?,
	val reasonCode: Int?
	)