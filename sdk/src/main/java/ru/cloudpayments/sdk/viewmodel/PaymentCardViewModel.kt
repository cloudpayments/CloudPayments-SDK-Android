package ru.cloudpayments.sdk.viewmodel

import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Job
import ru.cloudpayments.sdk.api.CloudpaymentsApi
import javax.inject.Inject

internal class PaymentCardViewModel: BaseViewModel<PaymentCardViewState>() {
	override var currentState = PaymentCardViewState()
	override val viewState: MutableLiveData<PaymentCardViewState> by lazy {
		MutableLiveData(currentState)
	}

	private var disposable: Job? = null

	@Inject lateinit var api: CloudpaymentsApi

	override fun onCleared() {
		super.onCleared()

		disposable?.cancel()
	}
}

internal data class PaymentCardViewState(val a: String? = null): BaseViewState()