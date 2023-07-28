package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable

data class QrLinkStatusWaitResponse(
	@SerializedName("Success") val success: Boolean?,
	@SerializedName("Message") val message: String?,
	@SerializedName("Model") val transaction: QrLinkStatusWait?) {
	fun handleError(): Observable<QrLinkStatusWaitResponse> {
		return if (success == true ){
			Observable.just(this)
		} else {
			Observable.error(CloudpaymentsTransactionError(message ?: ""))
		}
	}
}