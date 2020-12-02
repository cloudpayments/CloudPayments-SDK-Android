package ru.cloudpayments.sdk.util

import ru.cloudpayments.sdk.configuration.PaymentData
import ru.cloudpayments.sdk.viewmodel.PaymentProcessViewModelFactory

internal object InjectorUtils {
    fun providePaymentProcessViewModelFactory(paymentData: PaymentData, cryptogram: String, email: String): PaymentProcessViewModelFactory {
        return PaymentProcessViewModelFactory(paymentData, cryptogram, email)
    }
}