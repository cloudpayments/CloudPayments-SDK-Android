package ru.cloudpayments.sdk.configuration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.cloudpayments.sdk.api.models.PaymentDataPayer

@Parcelize
class PaymentData(
	val amount: String,
	var currency: String = "RUB",
	val invoiceId: String? = null,
	val description: String? = null,
	val accountId: String? = null,
	val email: String? = null,
	val payer: PaymentDataPayer? = null,
	val jsonData: String? = null
) : Parcelable