package ru.cloudpayments.sdk.viewmodel

import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import javax.inject.Inject

internal class PaymentCardViewModel: BaseViewModel<PaymentCardViewState>() {
	override var currentState = PaymentCardViewState()
	override val viewState: StateFlow<PaymentCardViewState> by lazy {
		MutableStateFlow(currentState)
	}

	private var disposable: Job? = null

	@Inject lateinit var api: CloudpaymentsApi

	override fun onCleared() {
		super.onCleared()

		disposable?.cancel()
	}
}

internal data class PaymentCardViewState(val a: String? = null): BaseViewState()