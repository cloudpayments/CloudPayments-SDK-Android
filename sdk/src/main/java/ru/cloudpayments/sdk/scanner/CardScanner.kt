package ru.cloudpayments.sdk.scanner

import android.content.Context
import android.content.Intent
import android.os.Parcelable

abstract class CardScanner: Parcelable {
	abstract fun getScannerIntent(context: Context): Intent?
	abstract fun getCardDataFromIntent(data: Intent): CardData?
}