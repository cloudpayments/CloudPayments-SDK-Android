package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName

data class ThreeDsRequestBody(
	@SerializedName("MD") val md: String,
	@SerializedName("PaRes") val paRes: String)