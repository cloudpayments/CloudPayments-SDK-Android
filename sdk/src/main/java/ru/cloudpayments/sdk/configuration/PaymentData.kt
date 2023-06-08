package ru.cloudpayments.sdk.configuration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.cloudpayments.sdk.api.models.PaymentDataPayer

@Parcelize
class PaymentData(val amount: String,
				  var currency: String = "RUB",
				  val invoiceId: String? = null,
				  val description: String? = null,
				  val accountId: String? = null,
				  var email: String? = null,
				  val payer: PaymentDataPayer? = null,
				  val jsonData: String? = null): Parcelable