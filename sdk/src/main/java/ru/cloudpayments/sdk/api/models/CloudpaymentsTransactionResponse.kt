package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName

data class CloudpaymentsTransactionResponse(
	@SerializedName("Success") val success: Boolean?,
	@SerializedName("Message") val message: String?,
	@SerializedName("Model") val transaction: CloudpaymentsTransaction?) {
	fun handleError(): CloudpaymentsTransactionResponse {
		return if (success == true || (!transaction?.acsUrl.isNullOrEmpty() && !transaction?.paReq.isNullOrEmpty())){
			this
		} else {
			throw CloudpaymentsTransactionError(message ?: transaction?.cardHolderMessage.orEmpty())
		}
	}
}