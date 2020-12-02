package ru.cloudpayments.sdk.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.cloudpayments.sdk.configuration.PaymentData

internal class PaymentProcessViewModelFactory(private val paymentData: PaymentData,
									 private val cryptogram: String,
									 private val email: String): ViewModelProvider.Factory {
	@Suppress("UNCHECKED_CAST")
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		return PaymentProcessViewModel(paymentData, cryptogram, email) as T
	}
}