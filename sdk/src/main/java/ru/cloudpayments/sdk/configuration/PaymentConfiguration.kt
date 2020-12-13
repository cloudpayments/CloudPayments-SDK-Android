package ru.cloudpayments.sdk.configuration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import ru.cloudpayments.sdk.scanner.CardScanner

@Parcelize
class PaymentConfiguration(val paymentData: PaymentData,
						   val scanner: CardScanner?): Parcelable