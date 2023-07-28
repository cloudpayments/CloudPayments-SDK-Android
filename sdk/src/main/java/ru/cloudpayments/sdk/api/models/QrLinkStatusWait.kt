package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName

data class QrLinkStatusWait(
	@SerializedName("TransactionId") val transactionId: Int?,
	@SerializedName("Status") val status: String?,
	@SerializedName("StatusCode") val statusCode: String?)


