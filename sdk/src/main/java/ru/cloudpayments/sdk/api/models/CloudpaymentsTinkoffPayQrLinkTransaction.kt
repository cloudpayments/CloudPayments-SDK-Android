package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName

data class CloudpaymentsTinkoffPayQrLinkTransaction(
	@SerializedName("TransactionId") val transactionId: Int?,
	@SerializedName("ProviderQrId") val providerQrId: String?,
	@SerializedName("QrUrl") val qrUrl: String?)