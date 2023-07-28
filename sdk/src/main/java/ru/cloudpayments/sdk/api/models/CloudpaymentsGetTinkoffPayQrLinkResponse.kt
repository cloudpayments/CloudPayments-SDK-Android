package ru.cloudpayments.sdk.api.models

import com.google.gson.annotations.SerializedName
import io.reactivex.Observable

data class CloudpaymentsGetTinkoffPayQrLinkResponse(
	@SerializedName("Success") val success: Boolean?,
	@SerializedName("Message") val message: String?,
	@SerializedName("Model") val transaction: CloudpaymentsTinkoffPayQrLinkTransaction?) {
	fun handleError(): Observable<CloudpaymentsGetTinkoffPayQrLinkResponse> {
		return if (success == true ) {
			Observable.just(this)
		} else {
			Observable.error(CloudpaymentsTransactionError(message ?: ""))
		}
	}
}