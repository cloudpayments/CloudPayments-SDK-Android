package ru.cloudpayments.sdk.configuration

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import ru.cloudpayments.sdk.scanner.CardScanner

@Parcelize
data class PaymentConfiguration(
	val publicId: String,
	val paymentData: PaymentData,
	val scanner: CardScanner?,
	val showEmailField: Boolean = false,
	val useDualMessagePayment: Boolean = false,
	val apiUrl: String = ""
) : Parcelable