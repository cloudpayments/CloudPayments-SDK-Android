package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName

data class QrLinkStatusWaitBody(
	@SerializedName("TransactionId") val transactionId: Int)