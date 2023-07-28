package ru.cloudpayments.sdk.configuration

import android.os.Parcelable
import android.util.Log
import com.google.gson.GsonBuilder
import com.google.gson.JsonSyntaxException
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import ru.cloudpayments.sdk.Constants
import ru.cloudpayments.sdk.api.models.PaymentDataPayer
import ru.cloudpayments.sdk.util.TAG

@Parcelize
class PaymentData(
	val amount: String,
	var currency: String = "RUB",
	val invoiceId: String? = null,
	val description: String? = null,
	val accountId: String? = null,
	var email: String? = null,
	val payer: PaymentDataPayer? = null,
	val jsonData: String? = null
) : Parcelable {

	fun jsonDataHasRecurrent(): Boolean {

		if (!jsonData.isNullOrEmpty()) {
			val gson = GsonBuilder()
				.setLenient()
				.create()

			try {
				val cpJsonData = gson.fromJson(jsonData, CpJsonData::class.java)
				cpJsonData.cloudPayments?.recurrent?.interval?.let {
					return true
				}
			} catch (e: JsonSyntaxException) {
				Log.e(TAG, "JsonData syntax error")
			}
		}
		return false
	}
}

data class CpJsonData(
	@SerializedName("cloudPayments") val cloudPayments: CloudPaymentsJsonData?
)

data class CloudPaymentsJsonData(
	@SerializedName("recurrent") val recurrent: CloudPaymentsRecurrentJsonData?
)

data class CloudPaymentsRecurrentJsonData(
	@SerializedName("interval") val interval: String?,
	@SerializedName("period") val period: String?,
	@SerializedName("amount") val amount: String?
)
