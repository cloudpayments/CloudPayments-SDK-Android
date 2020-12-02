package ru.cloudpayments.sdk.configuration

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class PaymentData(val publicId: String, val amount: String, val currency: String): Parcelable