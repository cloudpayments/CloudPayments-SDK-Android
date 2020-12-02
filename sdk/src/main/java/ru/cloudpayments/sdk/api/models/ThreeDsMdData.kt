package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ThreeDsMdData(
	@SerializedName("TransactionId") val transactionId: String,
	@SerializedName("ThreeDsCallbackId") val threeDsCallbackId: String,
	@SerializedName("SuccessUrl") val successURL: String,
	@SerializedName("FailUrl") val failURL: String): Serializable