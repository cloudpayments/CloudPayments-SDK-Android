package ru.cloudpayments.sdk.util

import com.google.android.gms.wallet.WalletConstants
import ru.cloudpayments.sdk.BuildConfig

val GOOGLE_PAY_ENVIRONMENT = if (BuildConfig.DEBUG)
	WalletConstants.ENVIRONMENT_TEST else
	WalletConstants.ENVIRONMENT_PRODUCTION