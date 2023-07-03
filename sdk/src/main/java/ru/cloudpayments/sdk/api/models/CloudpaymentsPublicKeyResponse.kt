package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName

data class CloudpaymentsPublicKeyResponse(
		@SerializedName("Pem") val pem: String?,
		@SerializedName("Version") val version: Int?)

