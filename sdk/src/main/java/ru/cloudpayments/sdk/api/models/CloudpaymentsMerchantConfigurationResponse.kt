package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName

data class CloudpaymentsMerchantConfigurationResponse(
	@SerializedName("Success") val success: Boolean?,
	@SerializedName("Message") val message: String?,
	@SerializedName("Model") val model: MerchantConfiguration?
)

data class MerchantConfiguration(
	@SerializedName("ExternalPaymentMethods") val externalPaymentMethods: ArrayList<ExternalPaymentMethods>?,
	@SerializedName("Features") val features: Features?
)

data class ExternalPaymentMethods(
	@SerializedName("Type") val type: Int?,
	@SerializedName("Enabled") val enabled: Boolean?
)

data class Features(
	@SerializedName("IsSaveCard") val isSaveCard: Int?
)