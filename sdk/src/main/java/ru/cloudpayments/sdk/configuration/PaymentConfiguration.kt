package ru.cloudpayments.sdk.configuration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
<<<<<<< HEAD

@Parcelize
class PaymentConfiguration(val paymentData: PaymentData): Parcelable
=======
import ru.cloudpayments.sdk.scanner.CardScanner

@Parcelize
class PaymentConfiguration(val paymentData: PaymentData,
						   val scanner: CardScanner?): Parcelable
>>>>>>> master
