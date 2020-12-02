package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable

data class CloudpaymentsTransactionResponse(
	@SerializedName("Success") val success: Boolean?,
	@SerializedName("Message") val message: String?,
	@SerializedName("Model") val transaction: CloudpaymentsTransaction?) {
	fun handleError(): Observable<CloudpaymentsTransactionResponse> {
		return if (success == true || (!transaction?.acsUrl.isNullOrEmpty() && !transaction?.paReq.isNullOrEmpty())){
			Observable.just(this)
		} else {
			Observable.error(CloudpaymentsTransactionError(message ?: transaction?.cardHolderMessage.orEmpty()))
		}
	}
}