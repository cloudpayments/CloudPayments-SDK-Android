package ru.cloudpayments.sdk.scanner

import android.content.Context
import android.content.Intent
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import java.io.Serializable

abstract class CardScanner: Parcelable {
	abstract fun getScannerIntent(context: Context): Intent?
	abstract fun getCardDataFromIntent(data: Intent): CardData?
}