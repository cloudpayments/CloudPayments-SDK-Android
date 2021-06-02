package ru.cloudpayments.sdk.configuration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import kotlinx.android.parcel.RawValue

@Parcelize
class PaymentData(val publicId: String,
				  val amount: String,
				  val currency: String,
				  val cardholderName: String? = null,
				  val description: String? = null,
				  val accountId: String? = null,
				  val invoiceId: String? = null,
				  val ipAddress: String? = null,
				  val cultureName: String? = null,
				  val payer: String? = null,
				  val jsonData: @RawValue HashMap<String, Any>? = null): Parcelable